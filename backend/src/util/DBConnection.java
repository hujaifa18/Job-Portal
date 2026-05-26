package util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * DBConnection — Fixed thread-safe connection pool.
 *
 * BUGS FIXED (vs original):
 *  1. Original ArrayList pool returned the SAME connection to concurrent threads
 *     → SQL state corruption on concurrent requests.
 *  2. Pool now uses BlockingQueue for atomic borrow/return semantics.
 *  3. Connections wrap close() via dynamic proxy so try-with-resources in DAOs
 *     returns the connection to the pool instead of closing it for real.
 *  4. Port auto-detection kept (3306 → 3307 → 3308).
 */
public class DBConnection {

    private static final String HOST     = "127.0.0.1";
    private static final String DATABASE = "jobportal_db";
    private static final int[]  PORTS    = {3306, 3307, 3308};
    private static final String USER     = "root";
    private static final String PASSWORD = "";
    private static final String OPTIONS  = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final int POOL_SIZE   = 10;
    private static final int TIMEOUT_SEC = 5;

    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static volatile String connectedUrl = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            for (int port : PORTS) {
                String url = "jdbc:mysql://" + HOST + ":" + port + "/" + DATABASE + OPTIONS;
                try {
                    System.out.println("[DB] Trying port " + port + "...");
                    Connection c = DriverManager.getConnection(url, USER, PASSWORD);
                    c.close(); // test only
                    connectedUrl = url;
                    System.out.println("[DB] Connected on port " + port);
                    break;
                } catch (SQLException e) {
                    System.out.println("[DB] Port " + port + " failed: " + e.getMessage());
                }
            }

            if (connectedUrl == null) {
                System.err.println("[DB] FATAL: Cannot connect to MySQL on any port.");
                System.err.println("[DB] Start XAMPP/MySQL then restart the server.");
            } else {
                for (int i = 0; i < POOL_SIZE; i++) {
                    pool.offer(DriverManager.getConnection(connectedUrl, USER, PASSWORD));
                }
                System.out.println("[DB] Pool ready (" + pool.size() + " connections).");
            }

        } catch (Exception e) {
            System.err.println("[DB] Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Borrow a connection. The returned Connection's close() method returns it
     * to the pool instead of physically closing it — so try-with-resources works.
     */
    public static Connection getConnection() throws SQLException {
        if (connectedUrl == null) {
            throw new SQLException("Database not connected. Start MySQL and restart the server.");
        }
        try {
            Connection real = pool.poll(TIMEOUT_SEC, TimeUnit.SECONDS);
            if (real == null || real.isClosed() || !real.isValid(2)) {
                // Replace dead connection
                real = DriverManager.getConnection(connectedUrl, USER, PASSWORD);
            }
            return wrapConnection(real);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted waiting for DB connection.", e);
        }
    }

    /** Wrap so close() → pool.offer() not physical close */
    private static Connection wrapConnection(Connection real) {
        return (Connection) Proxy.newProxyInstance(
            DBConnection.class.getClassLoader(),
            new Class[]{Connection.class},
            new PoolReturnHandler(real)
        );
    }

    private static class PoolReturnHandler implements InvocationHandler {
        private final Connection real;

        PoolReturnHandler(Connection real) { this.real = real; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                // Return to pool rather than closing
                pool.offer(real);
                return null;
            }
            if ("isClosed".equals(method.getName())) {
                return real.isClosed();
            }
            return method.invoke(real, args);
        }
    }
}

package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static final String HOST      = "127.0.0.1";
    private static final String DATABASE  = "jobportal_db";
    private static final int[] PORTS       = {3306, 3307, 3308};
    private static final String USER      = "root";
    private static final String PASSWORD  = "";
    private static final String OPTIONS   = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final int POOL_SIZE = 10;
    private static final List<Connection> pool = new ArrayList<>();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String lastUrl = null;
            SQLException lastException = null;
            boolean connected = false;

            for (int port : PORTS) {
                String url = "jdbc:mysql://" + HOST + ":" + port + "/" + DATABASE + OPTIONS;
                lastUrl = url;
                try {
                    System.out.println("Trying database URL: " + url);
                    Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
                    pool.add(conn);
                    connected = true;
                    break;
                } catch (SQLException e) {
                    lastException = e;
                    System.out.println("Connection failed on port " + port + ": " + e.getMessage());
                }
            }

            if (!connected) {
                throw lastException != null ? lastException : new SQLException("Unable to connect to MySQL on any configured port");
            }

            for (int i = pool.size(); i < POOL_SIZE; i++) {
                pool.add(DriverManager.getConnection(pool.get(0).getMetaData().getURL(), USER, PASSWORD));
            }

            System.out.println("Connection pool initialized (" + pool.size() + " connections)");
        } catch (Exception e) {
            System.out.println("Failed to initialize connection pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        for (Connection conn : pool) {
            try {
                if (conn != null && !conn.isClosed() && conn.isValid(1)) {
                    return conn;
                }
            } catch (SQLException ignored) {}
        }
        // All connections busy or invalid — create a fresh one
        String url = !pool.isEmpty() ? pool.get(0).getMetaData().getURL() : buildUrl(PORTS[0]);
        Connection fresh = DriverManager.getConnection(url, USER, PASSWORD);
        pool.add(fresh);
        return fresh;
    }

    private static String buildUrl(int port) {
        return "jdbc:mysql://" + HOST + ":" + port + "/" + DATABASE + OPTIONS;
    }
}
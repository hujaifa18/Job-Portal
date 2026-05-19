package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/jobportal_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static final int POOL_SIZE = 10;
    private static final List<Connection> pool = new ArrayList<>();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.add(DriverManager.getConnection(URL, USER, PASSWORD));
            }
            System.out.println("Connection pool initialized (" + POOL_SIZE + " connections)");
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
        Connection fresh = DriverManager.getConnection(URL, USER, PASSWORD);
        pool.add(fresh);
        return fresh;
    }
}
package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        try {
            // Local MySQL Server Configuration
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/jobportal_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root",
                ""
            );

            System.out.println("Database Connected ✅");
            return conn;

        } catch (Exception e) {
            System.out.println("Connection Failed ❌");
            e.printStackTrace();
        }

        return null;
    }
}
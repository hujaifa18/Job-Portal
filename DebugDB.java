import java.sql.*;
public class DebugDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/jobportal_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String pass = "";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("USER_COUNT=" + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

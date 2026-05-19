package dao;

import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    // ================= REGISTER USER =================
    public boolean register(User user) {

        // Reject if email already taken
        if (emailExists(user.getEmail())) return false;

        String sql = "INSERT INTO users(name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Register error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ================= EMAIL EXISTS CHECK =================
    public boolean emailExists(String email) {

        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= LOGIN USER =================
    // Returns role string on success, null on failure
    public String login(String email, String password) {

        String sql = "SELECT role FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ================= GET USER PROFILE =================
    public Map<String, String> getUserByEmail(String email) {

        String sql = "SELECT id, name, email, role FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> user = new HashMap<>();
                    user.put("id",    String.valueOf(rs.getInt("id")));
                    user.put("name",  rs.getString("name"));
                    user.put("email", rs.getString("email"));
                    user.put("role",  rs.getString("role"));
                    return user;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= UPDATE PROFILE =================
    public boolean updateProfile(String email, String newName, String newPassword) {

        String sql;
        boolean changePassword = newPassword != null && !newPassword.isEmpty();

        if (changePassword) {
            sql = "UPDATE users SET name = ?, password = ? WHERE email = ?";
        } else {
            sql = "UPDATE users SET name = ? WHERE email = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (changePassword) {
                ps.setString(1, newName);
                ps.setString(2, newPassword);
                ps.setString(3, email);
            } else {
                ps.setString(1, newName);
                ps.setString(2, email);
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
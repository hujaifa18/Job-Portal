package service;

import dao.UserDAO;
import model.User;
import java.util.Map;

public class UserService {

    private final UserDAO dao = new UserDAO();

    public boolean register(String name, String email, String password, String role) {

        if (name == null || name.trim().isEmpty()) return false;
        if (email == null || !email.contains("@")) return false;
        if (password == null || password.length() < 6) return false;
        if (!role.equals("candidate") && !role.equals("recruiter")) return false;

        return dao.register(new User(name.trim(), email.trim(), password, role));
    }

    public String login(String email, String password) {

        if (email == null || password == null) return null;
        return dao.login(email.trim(), password);
    }

    public Map<String, String> getProfile(String email) {
        return dao.getUserByEmail(email);
    }

    public boolean updateProfile(String email, String name, String newPassword) {
        if (name == null || name.trim().isEmpty()) return false;
        return dao.updateProfile(email, name.trim(), newPassword);
    }
}
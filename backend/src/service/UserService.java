package service;

import dao.UserDAO;
import model.User;

public class UserService {

    public boolean register(String name, String email, String password, String role) {

        User user = new User(name, email, password, role);

        return new UserDAO().register(user);
    }
}
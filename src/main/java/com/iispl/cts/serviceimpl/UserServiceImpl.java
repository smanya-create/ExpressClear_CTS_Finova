package com.iispl.cts.serviceimpl;

import com.iispl.cts.dao.UserDAO;
import com.iispl.cts.daoimpl.UserDAOImpl;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.UserService;
import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

import java.util.List;

public class UserServiceImpl implements UserService {

    private static UserServiceImpl instance;
    private final UserDAO userDAO = UserDAOImpl.getInstance();

    public static synchronized UserServiceImpl getInstance() {
        if (instance == null) instance = new UserServiceImpl();
        return instance;
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || user.getPassword() == null) {
            return null;
        }

        // Only allow ACTIVE users to log in
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }

        String dbPassword = user.getPassword().trim();

        // 1. Check if DB password is a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
            try {
                if (BCrypt.checkpw(rawPassword, dbPassword)) {
                    return user;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        // 2. Fallback to direct comparison if stored as plain text
        else if (dbPassword.equals(rawPassword)) {
            return user;
        }

        return null; // Password mismatch
    }

    @Override
    public String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            return plainTextPassword;
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));
    }

    @Override
    public boolean registerOrUpdateUser(User user, String plainTextPassword) {
        if (plainTextPassword != null && !plainTextPassword.trim().isEmpty()) {
            user.setPassword(hashPassword(plainTextPassword));
        }
        return userDAO.saveUser(user);
    }

    @Override
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    public User findByEmployeeId(String employeeId) {
        return userDAO.findByEmployeeId(employeeId);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    @Override
    public List<User> searchUsers(String query, String roleId, String status) {
        return userDAO.searchUsers(query, roleId, status);
    }

    @Override
    public String generateNextUserId() {
        return userDAO.generateNextUserId();
    }

    @Override
    public String generateNextEmployeeId() {
        return userDAO.generateNextEmployeeId();
    }
}
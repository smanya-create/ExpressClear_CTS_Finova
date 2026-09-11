package com.iispl.cts.serviceimpl;

import com.iispl.cts.dao.UserDAO;
import com.iispl.cts.daoimpl.UserDAOImpl;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserServiceImpl implements UserService {

    private static UserServiceImpl instance;
    private final UserDAO userDAO = UserDAOImpl.getInstance();

    public static synchronized UserServiceImpl getInstance() {
        if (instance == null) instance = new UserServiceImpl();
        return instance;
    }

    @Override
    public User authenticate(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null) {
            return null;
        }

        String trimmedId = identifier.trim();

        // 1. Look up user by username first, fallback to email if not found
        User user = userDAO.findByUsername(trimmedId);
        if (user == null && trimmedId.contains("@")) {
            user = userDAO.findByEmail(trimmedId);
        }

        if (user == null || user.getPassword() == null) {
            return null;
        }

        String dbPassword = user.getPassword().trim();
        boolean passwordMatches = false;

        // 2. Check if DB password is a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
            try {
                passwordMatches = BCrypt.checkpw(rawPassword, dbPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        // 3. Fallback to direct comparison if stored as plain text
        else if (dbPassword.equals(rawPassword)) {
            passwordMatches = true;
        }

        if (!passwordMatches) {
            return null; // Password mismatch
        }

        return user;
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

    @Override
    public List<User> findUsersByRoleId(String roleId) {
        if (roleId == null || roleId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        String trimmedRoleId = roleId.trim();
        List<User> users = userDAO.findUsersByRoleId(trimmedRoleId);
        return users != null ? users : java.util.Collections.emptyList();
    }
}
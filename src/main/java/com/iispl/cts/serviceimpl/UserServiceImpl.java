package com.iispl.cts.serviceimpl;

import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

import com.iispl.cts.dao.UserDAO;
import com.iispl.cts.daoimpl.UserDAOImpl;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.UserService;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null || username.trim().isEmpty() || rawPassword.isEmpty()) {
            System.out.println("[UserServiceImpl] Authentication aborted: Username or password input is empty.");
            return null;
        }

        User user = userDAO.findByUsername(username.trim());

        if (user != null && user.getPassword() != null) {
            String dbPassword = user.getPassword().trim();
            String inputPassword = rawPassword.trim();

            System.out.println("[UserServiceImpl] DB Stored Password: " + dbPassword);
            System.out.println("[UserServiceImpl] Entered Input Password: " + inputPassword);

            try {
                // Check 1: BCrypt verification
                if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
                    boolean isMatch = BCrypt.checkpw(inputPassword, dbPassword);
                    System.out.println("[UserServiceImpl] BCrypt Match Result: " + isMatch);
                    if (isMatch) {
                        return user;
                    }
                } 
                
                // Check 2: Fallback Plain Text verification
                if (inputPassword.equals(dbPassword)) {
                    System.out.println("[UserServiceImpl] Plain-text Match Result: true");
                    return user;
                }

            } catch (Exception e) {
                System.err.println("[UserServiceImpl] Password verification error: " + e.getMessage());
            }
        }

        System.out.println("[UserServiceImpl] Authentication Failed for user: " + username);
        return null;
    }

    @Override
    public String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainTextPassword.trim(), BCrypt.gensalt(10));
    }

    @Override
    public boolean registerOrUpdateUser(User user, String plainTextPassword) {
        if (user == null) {
            return false;
        }

        if (plainTextPassword != null && !plainTextPassword.trim().isEmpty()) {
            user.setPassword(hashPassword(plainTextPassword));
        }

        return userDAO.saveUser(user);
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userDAO.findByUsername(username.trim());
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}
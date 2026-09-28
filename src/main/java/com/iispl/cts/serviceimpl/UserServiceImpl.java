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

    // Singleton instance access
    public static synchronized UserServiceImpl getInstance() {
        if (instance == null) instance = new UserServiceImpl();
        return instance;
    }

    // Authenticates against hashed bcrypt password with fallback for legacy plain text
    @Override
    public User authenticate(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null) {
            return null;
        }

        User user = userDAO.findByIdentifier(identifier);

        if (user == null || user.getPassword() == null) {
            return null;
        }

        String dbPassword = user.getPassword().trim();
        boolean passwordMatches = false;

        // Verify using BCrypt if stored hash contains standard salt prefix
        if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
            try {
                passwordMatches = BCrypt.checkpw(rawPassword, dbPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (dbPassword.equals(rawPassword)) {
            passwordMatches = true;
        }

        return passwordMatches ? user : null;
    }

    // Hashes plain text input using BCrypt with workload factor 10
    @Override
    public String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            return plainTextPassword;
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(10));
    }

    // Hashes new password if provided before persisting user record
    @Override
    public boolean registerOrUpdateUser(User user, String plainTextPassword) {
        if (plainTextPassword != null && !plainTextPassword.trim().isEmpty()) {
            user.setPassword(hashPassword(plainTextPassword));
        }
        return userDAO.saveUser(user);
    }

    // Looks up user by unique username
    @Override
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    // Looks up user by assigned employee identifier
    @Override
    public User findByEmployeeId(String employeeId) {
        return userDAO.findByEmployeeId(employeeId);
    }

    // Fetches all system users
    @Override
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    // Searches users by keyword, role filter, and active/inactive status
    @Override
    public List<User> searchUsers(String query, String roleId, String status) {
        return userDAO.searchUsers(query, roleId, status);
    }

    // Generates the next sequential user system ID
    @Override
    public String generateNextUserId() {
        return userDAO.generateNextUserId();
    }

    // Generates the next sequential employee identifier
    @Override
    public String generateNextEmployeeId() {
        return userDAO.generateNextEmployeeId();
    }

    // Retrieves all user accounts mapped to a specific role ID
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
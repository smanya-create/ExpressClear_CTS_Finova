package com.iispl.cts.dao;

import java.util.List;
import com.iispl.cts.entity.User;

public interface UserDAO {

    /**
     * Retrieves an active user record by username.
     * 
     * @param username The username to search for
     * @return User object if found and active, null otherwise
     */
    User findByUsername(String username);

    /**
     * Retrieves a list of all active users.
     * 
     * @return List of User objects
     */
    List<User> getAllUsers();

    /**
     * Saves or updates a user record.
     * 
     * @param user The user to save
     * @return true if successful, false otherwise
     */
    boolean saveUser(User user);
}
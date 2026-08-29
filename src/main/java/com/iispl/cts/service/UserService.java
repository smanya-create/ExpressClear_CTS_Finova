package com.iispl.cts.service;

import java.util.List;
import com.iispl.cts.entity.User;

public interface UserService {

    /**
     * Authenticates a user using BCrypt hash comparison (or legacy plain-text fallback).
     * 
     * @param username    The entered username
     * @param rawPassword The entered plain-text password
     * @return User object if authentication succeeds, null otherwise
     */
    User authenticate(String username, String rawPassword);

    /**
     * Hashes a plain-text password using BCrypt.
     * 
     * @param plainTextPassword The raw password
     * @return Hashed BCrypt string
     */
    String hashPassword(String plainTextPassword);

    /**
     * Registers or updates a user record, automatically hashing the password if provided.
     * 
     * @param user              The user entity to save
     * @param plainTextPassword The raw password to hash and set
     * @return true if successful, false otherwise
     */
    boolean registerOrUpdateUser(User user, String plainTextPassword);

    /**
     * Retrieves a user by username.
     * 
     * @param username The username to look up
     * @return User model object
     */
    User findByUsername(String username);

    /**
     * Retrieves all active users.
     * 
     * @return List of active users
     */
    List<User> getAllUsers();
}
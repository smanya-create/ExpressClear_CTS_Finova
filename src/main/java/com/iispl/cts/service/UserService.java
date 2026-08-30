package com.iispl.cts.service;

import java.util.List;
import com.iispl.cts.entity.User;

public interface UserService {
	User authenticate(String username, String rawPassword);
    String hashPassword(String plainTextPassword);
    boolean registerOrUpdateUser(User user, String plainTextPassword);
    User findByUsername(String username);
    User findByEmployeeId(String employeeId);
    List<User> getAllUsers();
    List<User> searchUsers(String query, String roleId, String status);
    String generateNextUserId();
    String generateNextEmployeeId();
}
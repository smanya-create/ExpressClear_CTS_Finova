package com.iispl.cts.dao;

import java.util.List;
import com.iispl.cts.entity.User;

public interface UserDAO {

	User findByUsername(String username);
    User findByEmployeeId(String employeeId);
    List<User> getAllUsers();
    List<User> searchUsers(String query, String roleId, String status);
    boolean saveUser(User user);
    String generateNextUserId();
    String generateNextEmployeeId();
}
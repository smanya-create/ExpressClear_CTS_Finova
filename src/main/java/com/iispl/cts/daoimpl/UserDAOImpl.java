package com.iispl.cts.daoimpl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.UserDAO;
import com.iispl.cts.entity.User;

public class UserDAOImpl implements UserDAO {

    @Override
    public User findByUsername(String username) {
        // Joins the role table to map role_name (e.g., 'ADMIN', 'OUTWARD_MAKER') into setRoleId
        String sql = "SELECT u.user_id, u.employee_id, u.username, u.password, " +
                     "u.full_name, u.email, u.mobile_number, u.status, u.user_created_at, " +
                     "r.role_name " +
                     "FROM users u " +
                     "JOIN role r ON u.role_id = r.role_id " +
                     "WHERE LOWER(u.username) = LOWER(?) AND UPPER(u.status) = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    System.out.println("[DAO] Successfully retrieved user: " + user.getUsername() + 
                                       " with Role: " + user.getRoleId());
                    return user;
                } else {
                    System.out.println("[DAO] No active user found for username: " + username);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] Exception in findByUsername:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT u.user_id, u.employee_id, u.username, u.password, " +
                     "u.full_name, u.email, u.mobile_number, u.status, u.user_created_at, " +
                     "r.role_name " +
                     "FROM users u " +
                     "JOIN role r ON u.role_id = r.role_id " +
                     "WHERE UPPER(u.status) = 'ACTIVE' " +
                     "ORDER BY u.username ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                userList.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] Exception in getAllUsers:");
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (user_id, role_id, employee_id, username, password, full_name, email, mobile_number, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (user_id) DO UPDATE SET " +
                     "username = EXCLUDED.username, " +
                     "password = EXCLUDED.password, " +
                     "status = EXCLUDED.status, " +
                     "role_id = EXCLUDED.role_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getRoleId()); // expects role_id (e.g., 'ROL1001')
            ps.setString(3, user.getEmployeeId());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getFullName());
            ps.setString(7, user.getEmail());
            ps.setString(8, user.getMobileNumber());
            ps.setString(9, user.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO Error] Exception in saveUser:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps ResultSet row to User model object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        
        // Map role_name ('ADMIN', 'OUTWARD_MAKER', etc.) into roleId field for easy UI validation
        user.setRoleId(rs.getString("role_name")); 
        
        user.setEmployeeId(rs.getString("employee_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setMobileNumber(rs.getString("mobile_number"));
        user.setStatus(rs.getString("status"));
        user.setUserCreatedAt(rs.getTimestamp("user_created_at"));
        return user;
    }
}

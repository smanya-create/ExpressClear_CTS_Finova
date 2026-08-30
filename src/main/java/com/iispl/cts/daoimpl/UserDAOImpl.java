package com.iispl.cts.daoimpl;

import com.iispl.cts.dao.UserDAO;
import com.iispl.cts.entity.User;
import com.iispl.cts.common.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static UserDAOImpl instance;

    public static synchronized UserDAOImpl getInstance() {
        if (instance == null) instance = new UserDAOImpl();
        return instance;
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM users WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return searchUsers(null, "ALL", "ALL");
    }

    @Override
    public List<User> searchUsers(String query, String roleId, String status) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(employee_id) LIKE ? OR LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (roleId != null && !"ALL".equalsIgnoreCase(roleId)) {
            sql.append("AND role_id = ? ");
            params.add(roleId);
        }

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND LOWER(status) = ? ");
            params.add(status.toLowerCase());
        }

        sql.append("ORDER BY employee_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean saveUser(User user) {
        String checkSql = "SELECT user_id FROM users WHERE user_id = ?";
        String insertSql = "INSERT INTO users (user_id, role_id, employee_id, username, password, full_name, email, mobile_number, status, user_created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE users SET role_id = ?, status = ?, full_name = ?, email = ?, mobile_number = ?, password = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            boolean exists = false;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, user.getUserId());
                try (ResultSet rs = checkPs.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, user.getRoleId());
                    ps.setString(2, user.getStatus());
                    ps.setString(3, user.getFullName());
                    ps.setString(4, user.getEmail());
                    ps.setString(5, user.getMobileNumber());
                    ps.setString(6, user.getPassword());
                    ps.setString(7, user.getUserId());
                    return ps.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, user.getUserId());
                    ps.setString(2, user.getRoleId());
                    ps.setString(3, user.getEmployeeId());
                    ps.setString(4, user.getUsername());
                    ps.setString(5, user.getPassword());
                    ps.setString(6, user.getFullName());
                    ps.setString(7, user.getEmail());
                    ps.setString(8, user.getMobileNumber());
                    ps.setString(9, user.getStatus());
                    ps.setTimestamp(10, user.getUserCreatedAt() != null ? user.getUserCreatedAt() : new Timestamp(System.currentTimeMillis()));
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateNextUserId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(user_id, 4) AS INTEGER)) FROM users WHERE user_id LIKE 'USR%'";
        int max = 1000;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getObject(1) != null) {
                max = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "USR" + (max + 1);
    }

    @Override
    public String generateNextEmployeeId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(employee_id, 4) AS INTEGER)) FROM users WHERE employee_id LIKE 'EMP%'";
        int max = 1000;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getObject(1) != null) {
                max = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "EMP" + (max + 1);
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getString("user_id"));
        u.setRoleId(rs.getString("role_id"));
        u.setEmployeeId(rs.getString("employee_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setMobileNumber(rs.getString("mobile_number"));
        u.setStatus(rs.getString("status"));
        u.setUserCreatedAt(rs.getTimestamp("user_created_at"));
        return u;
    }
}
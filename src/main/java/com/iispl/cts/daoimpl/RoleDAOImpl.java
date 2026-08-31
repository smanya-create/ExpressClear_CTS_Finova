package com.iispl.cts.daoimpl;


import com.iispl.cts.dao.RoleDAO;
import com.iispl.cts.entity.Role;
import com.iispl.cts.common.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOImpl implements RoleDAO {

	private static RoleDAOImpl instance;

    public static synchronized RoleDAOImpl getInstance() {
        if (instance == null) instance = new RoleDAOImpl();
        return instance;
    }

   
  
    @Override
    public List<Role> searchRoles(String query, String status) {
        List<Role> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT role_id, role_name, description, status, permissions, role_created_at FROM role WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        // 1. Search Query filter (Role ID or Role Name)
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(role_id) LIKE ? OR LOWER(role_name) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
        }

        // 2. Status filter
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND LOWER(status) = ? ");
            params.add(status.trim().toLowerCase());
        }

        sql.append("ORDER BY role_id ASC");

        System.out.println("[DEBUG ROLES SQL] Executing: " + sql.toString());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Use mapResultSet to correctly read status, permissions, and description from DB
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ROLE DAO ERROR] Query failed:");
            e.printStackTrace();
        }

        System.out.println("[DEBUG ROLES LOADED] Count: " + list.size());
        return list;
    }

    private String resolveDefaultDescription(String roleName) {
        if (roleName == null) return "System access role";
        String upper = roleName.toUpperCase();
        if (upper.contains("ADMIN")) return "Full access to manage users, roles and system settings.";
        if (upper.contains("OUTWARD") && upper.contains("MAKER")) return "Allows user to create batches, enter cheque details and submit.";
        if (upper.contains("OUTWARD") && upper.contains("CHECKER")) return "Allows user to validate and verify batches submitted by maker.";
        if (upper.contains("INWARD") && upper.contains("MAKER")) return "Allows user to perform inward intake, repair, and data entry.";
        if (upper.contains("INWARD") && upper.contains("CHECKER")) return "Allows user to verify inward batches and generate return files.";
        return "Standard role access";
    }

    @Override
    public Role findById(String roleId) {
        String sql = "SELECT role_id, role_name, description, status, permissions, role_created_at FROM role WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Role findByName(String roleName) {
        String sql = "SELECT role_id, role_name, description, status, permissions, role_created_at FROM role WHERE LOWER(role_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName != null ? roleName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean saveRole(Role role) {
        String sql = "INSERT INTO role (role_id, role_name, description, status, permissions, role_created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getRoleId());
            ps.setString(2, role.getRoleName());
            ps.setString(3, role.getDescription());
            ps.setString(4, role.getStatus() != null ? role.getStatus() : "Active");
            ps.setString(5, role.getPermissions());
            ps.setTimestamp(6, role.getRoleCreatedAt() != null ? role.getRoleCreatedAt() : new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateRole(Role role) {
        String sql = "UPDATE role SET role_name = ?, description = ?, status = ?, permissions = ? WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.getRoleName());
            ps.setString(2, role.getDescription());
            ps.setString(3, role.getStatus());
            ps.setString(4, role.getPermissions());
            ps.setString(5, role.getRoleId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String generateNextRoleId() {
        String sql = "SELECT role_id FROM role WHERE role_id LIKE 'ROL%'";
        int maxNumber = 1000;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String roleId = rs.getString("role_id");
                if (roleId != null && roleId.length() > 3) {
                    try {
                        String numPart = roleId.substring(3).trim(); // strips 'ROL'
                        int num = Integer.parseInt(numPart);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String nextId = "ROL" + (maxNumber + 1);
        System.out.println("[DEBUG NEXT ROLE ID] Generated: " + nextId);
        return nextId;
    }

    private Role mapResultSet(ResultSet rs) throws SQLException {
        return new Role(
                rs.getString("role_id"),
                rs.getString("role_name"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("permissions"),
                rs.getTimestamp("role_created_at")
        );
    }
}
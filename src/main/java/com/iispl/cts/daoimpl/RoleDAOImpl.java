package com.iispl.cts.daoimpl;


import com.iispl.cts.dao.RoleDAO;
import com.iispl.cts.entity.Role;
import com.iispl.cts.common.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOImpl implements RoleDAO {

    private static RoleDAOImpl instance;

    public RoleDAOImpl() {}

    public static synchronized RoleDAOImpl getInstance() {
        if (instance == null) {
            instance = new RoleDAOImpl();
        }
        return instance;
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT role_id, role_name, role_created_at FROM roles ORDER BY role_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRole(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Role findById(String roleId) {
        String sql = "SELECT role_id, role_name, role_created_at FROM roles WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Role findByName(String roleName) {
        String sql = "SELECT role_id, role_name, role_created_at FROM roles WHERE LOWER(role_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName != null ? roleName.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRole(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        return new Role(
                rs.getString("role_id"),
                rs.getString("role_name"),
                rs.getTimestamp("role_created_at")
        );
    }
}
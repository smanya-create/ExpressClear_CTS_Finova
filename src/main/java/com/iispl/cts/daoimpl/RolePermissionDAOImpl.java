package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.RolePermissionDAO;

public class RolePermissionDAOImpl implements RolePermissionDAO {

	@Override
	public Set<String> getAssignedScreenIdsByRole(String roleId) {
		// TODO Auto-generated method stub
		Set<String> screenIds = new HashSet<>();
        String sql = "SELECT screen_id FROM role_screen_permission WHERE role_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    screenIds.add(rs.getString("screen_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenIds;
	}

	@Override
	public void saveRolePermissions(String roleId, Set<String> screenIds) throws Exception {
		// TODO Auto-generated method stub
		String deleteSql = "DELETE FROM role_screen_permission WHERE role_id = ?";
        String insertSql = "INSERT INTO role_screen_permission (role_id, screen_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete previous mappings
                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setString(1, roleId);
                    delPs.executeUpdate();
                }

                // 2. Batch insert selected screens
                if (screenIds != null && !screenIds.isEmpty()) {
                    try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                        for (String screenId : screenIds) {
                            insPs.setString(1, roleId);
                            insPs.setString(2, screenId);
                            insPs.addBatch();
                        }
                        insPs.executeBatch();
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

	}

	@Override
	public Set<String> getPermittedScreenCodesByRole(String roleId) {
		// TODO Auto-generated method stub
		Set<String> screenCodes = new HashSet<>();
        String sql = "SELECT s.screen_code FROM role_screen_permission rsp " +
                     "JOIN app_screen s ON rsp.screen_id = s.screen_id " +
                     "WHERE rsp.role_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    screenCodes.add(rs.getString("screen_code"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenCodes;
    }
	}



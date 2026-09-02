package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.NotificationDAO;
import com.iispl.cts.entity.Notification;

public class NotificationDAOImpl implements NotificationDAO {

    private static NotificationDAOImpl instance;

    public static synchronized NotificationDAOImpl getInstance() {
        if (instance == null) {
            instance = new NotificationDAOImpl();
        }
        return instance;
    }

    @Override
    public List<Notification> getUnreadByRoleOrUser(String role, String userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, recipient_role, recipient_user_id, message, is_read, created_at "
                   + "FROM notifications "
                   + "WHERE is_read = FALSE "
                   + "AND (LOWER(recipient_role) = LOWER(?) OR recipient_user_id = ?) "
                   + "ORDER BY created_at DESC LIMIT 15";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role != null ? role.trim() : "");
            ps.setString(2, userId != null ? userId.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                        rs.getLong("notification_id"),
                        rs.getString("recipient_role"),
                        rs.getString("recipient_user_id"),
                        rs.getString("message"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int getUnreadCount(String role, String userId) {
        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE is_read = FALSE "
                   + "AND (LOWER(recipient_role) = LOWER(?) OR recipient_user_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role != null ? role.trim() : "");
            ps.setString(2, userId != null ? userId.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean markAllAsRead(String role, String userId) {
        String sql = "UPDATE notifications SET is_read = TRUE "
                   + "WHERE is_read = FALSE "
                   + "AND (LOWER(recipient_role) = LOWER(?) OR recipient_user_id = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role != null ? role.trim() : "");
            ps.setString(2, userId != null ? userId.trim() : "");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createNotification(String targetRole, String targetUserId, String message) {
        String sql = "INSERT INTO notifications (recipient_role, recipient_user_id, message, is_read, created_at) "
                   + "VALUES (?, ?, ?, FALSE, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetRole != null ? targetRole.trim() : "");
            ps.setString(2, targetUserId != null ? targetUserId.trim() : null);
            ps.setString(3, message);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
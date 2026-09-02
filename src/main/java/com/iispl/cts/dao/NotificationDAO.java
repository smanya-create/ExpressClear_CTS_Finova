package com.iispl.cts.dao;

import java.util.List;

import com.iispl.cts.entity.Notification;

public interface NotificationDAO {
	List<Notification> getUnreadByRoleOrUser(String role, String userId);
    int getUnreadCount(String role, String userId);
    boolean markAllAsRead(String role, String userId);
    boolean createNotification(String targetRole, String targetUserId, String message);

}

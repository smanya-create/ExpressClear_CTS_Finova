package com.iispl.cts.serviceimpl;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.iispl.cts.dao.NotificationDAO;
import com.iispl.cts.daoimpl.NotificationDAOImpl;
import com.iispl.cts.entity.Notification;
import com.iispl.cts.service.NotificationService;

public class NotificationServiceImpl implements NotificationService {
	private static NotificationServiceImpl instance;
    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);

    private final NotificationDAO notificationDAO;
    
    private NotificationServiceImpl() {
        this.notificationDAO = NotificationDAOImpl.getInstance();
    }

    public static synchronized NotificationServiceImpl getInstance() {
        if (instance == null) {
            instance = new NotificationServiceImpl();
        }
        return instance;
    }

	@Override
	public List<Notification> getUnreadNotifications(String role, String userId) {
		// TODO Auto-generated method stub
		if ((role == null || role.trim().isEmpty()) && (userId == null || userId.trim().isEmpty())) {
            return Collections.emptyList();
        }
        try {
            return notificationDAO.getUnreadByRoleOrUser(role, userId);
        } catch (Exception e) {
            logger.error("Error retrieving unread notifications for role: {} and user: {}", role, userId, e);
            return Collections.emptyList();
        }
	}

	@Override
	public int getUnreadNotificationCount(String role, String userId) {
		// TODO Auto-generated method stub
		if ((role == null || role.trim().isEmpty()) && (userId == null || userId.trim().isEmpty())) {
            return 0;
        }
        try {
            return notificationDAO.getUnreadCount(role, userId);
        } catch (Exception e) {
            logger.error("Error fetching unread notification count for role: {} and user: {}", role, userId, e);
            return 0;
        }
	}

	@Override
	public boolean markAllNotificationsAsRead(String role, String userId) {
		// TODO Auto-generated method stub
		if ((role == null || role.trim().isEmpty()) && (userId == null || userId.trim().isEmpty())) {
            return false;
        }
        try {
            return notificationDAO.markAllAsRead(role, userId);
        } catch (Exception e) {
            logger.error("Error marking notifications as read for role: {} and user: {}", role, userId, e);
            return false;
        }
	}

	@Override
	public boolean sendNotification(String targetRole, String targetUserId, String message) {
		// TODO Auto-generated method stub
		if (message == null || message.trim().isEmpty()) {
            logger.warn("Attempted to send an empty notification message.");
            return false;
        }
        try {
            return notificationDAO.createNotification(targetRole, targetUserId, message.trim());
        } catch (Exception e) {
            logger.error("Error dispatching notification to role: {} / user: {}", targetRole, targetUserId, e);
            return false;
        }
	}

}

package com.iispl.cts.service;

import java.util.List;

import com.iispl.cts.entity.Notification;

public interface NotificationService {
	/**
	* Retrieves unread notifications for a specific user ID or role queue.
    */
   List<Notification> getUnreadNotifications(String role, String userId);

   /**
    * Gets the total count of unread notifications for badge rendering.
    */
   int getUnreadNotificationCount(String role, String userId);

   /**
    * Marks all notifications as read for the current role or user.
    */
   boolean markAllNotificationsAsRead(String role, String userId);

   /**
    * Sends a notification to a specific target role (e.g., OUTWARD_CHECKER)
    * or a specific user.
    */
   boolean sendNotification(String targetRole, String targetUserId, String message);

}

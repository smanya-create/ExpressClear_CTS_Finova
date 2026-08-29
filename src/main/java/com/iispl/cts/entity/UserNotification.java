package com.iispl.cts.entity;


import java.io.Serializable;
import java.sql.Timestamp;

public class UserNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    private String notificationId;
    private String userId;
    private String roleId;
    private String notificationType;
    private String notificationMessage;
    private boolean isRead;
    private Timestamp createdAt;
    private Timestamp readAt;

    public UserNotification() {
    }

    public UserNotification(String notificationId, String userId, String roleId, String notificationType, 
                            String notificationMessage, boolean isRead, Timestamp createdAt, Timestamp readAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.roleId = roleId;
        this.notificationType = notificationType;
        this.notificationMessage = notificationMessage;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getReadAt() {
		return readAt;
	}

	public void setReadAt(Timestamp readAt) {
		this.readAt = readAt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}
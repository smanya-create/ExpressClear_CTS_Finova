package com.iispl.cts.entity;

import java.sql.Timestamp;

public class Notification {
	
	private Long notificationId;
    private String recipientRole;
    private String recipientUserId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(Long notificationId, String recipientRole, String recipientUserId, 
                        String message, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.recipientRole = recipientRole;
        this.recipientUserId = recipientUserId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public String getRecipientRole() {
		return recipientRole;
	}

	public void setRecipientRole(String recipientRole) {
		this.recipientRole = recipientRole;
	}

	public String getRecipientUserId() {
		return recipientUserId;
	}

	public void setRecipientUserId(String recipientUserId) {
		this.recipientUserId = recipientUserId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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
    

}

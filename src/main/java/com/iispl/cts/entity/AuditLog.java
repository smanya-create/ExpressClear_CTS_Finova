package com.iispl.cts.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long auditId;
    private Timestamp timestamp;
    private String userId;
    private String username;
    private String roleName;
    private String module;
    private String action;
    private String details;
    private String ipAddress;
    private String status;

    public AuditLog() {}

    public AuditLog(Long auditId, Timestamp timestamp, String userId, String username, 
                    String roleName, String module, String action, String details, 
                    String ipAddress, String status) {
        this.auditId = auditId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.username = username;
        this.roleName = roleName;
        this.module = module;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.status = status;
    }

	public Long getAuditId() {
		return auditId;
	}

	public void setAuditId(Long auditId) {
		this.auditId = auditId;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

   
}
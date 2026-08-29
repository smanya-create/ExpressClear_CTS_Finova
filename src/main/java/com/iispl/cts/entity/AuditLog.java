package com.iispl.cts.entity;


import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auditId;
    private String userId;
    private String roleId;
    private String actionModule;
    private String actionType;
    private String actionDescription;
    private String ipAddress;
    private Timestamp createdAt;

    // Default Constructor
    public AuditLog() {
    }

    // Parameterized Constructor
    public AuditLog(String auditId, String userId, String roleId, String actionModule, 
                    String actionType, String actionDescription, String ipAddress, 
                    Timestamp createdAt) {
        this.auditId = auditId;
        this.userId = userId;
        this.roleId = roleId;
        this.actionModule = actionModule;
        this.actionType = actionType;
        this.actionDescription = actionDescription;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
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

    public String getActionModule() {
        return actionModule;
    }

    public void setActionModule(String actionModule) {
        this.actionModule = actionModule;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLog {" +
                "auditId='" + auditId + '\'' +
                ", userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                ", actionModule='" + actionModule + '\'' +
                ", actionType='" + actionType + '\'' +
                ", actionDescription='" + actionDescription + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
package com.iispl.cts.entity;


import java.io.Serializable;
import java.sql.Timestamp;

public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleId;
    private String roleName;
    private String description;
    private String status;
    private String permissions; // Comma-separated screen tokens
    private Timestamp roleCreatedAt;

    public Role() {}

    public Role(String roleId, String roleName, String description, String status, String permissions, Timestamp roleCreatedAt) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
        this.status = status;
        this.permissions = permissions;
        this.roleCreatedAt = roleCreatedAt;
    }
    // Getters and Setters

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public Timestamp getRoleCreatedAt() {
		return roleCreatedAt;
	}

	public void setRoleCreatedAt(Timestamp roleCreatedAt) {
		this.roleCreatedAt = roleCreatedAt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Role [roleId=" + roleId + ", roleName=" + roleName + ", description=" + description + ", status="
				+ status + ", permissions=" + permissions + ", roleCreatedAt=" + roleCreatedAt + "]";
	}

	
    
}

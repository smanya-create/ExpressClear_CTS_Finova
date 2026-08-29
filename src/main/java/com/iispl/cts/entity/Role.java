package com.iispl.cts.entity;


import java.io.Serializable;
import java.sql.Timestamp;

public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleId;
    private String roleName;
    private Timestamp roleCreatedAt;

    // Default Constructor
    public Role() {
    }

    // Parameterized Constructor
    public Role(String roleId, String roleName, Timestamp roleCreatedAt) {
        this.roleId = roleId;
        this.roleName = roleName;
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

    public Timestamp getRoleCreatedAt() {
        return roleCreatedAt;
    }

    public void setRoleCreatedAt(Timestamp roleCreatedAt) {
        this.roleCreatedAt = roleCreatedAt;
    }

    @Override
    public String toString() {
        return "Role {" +
                "roleId='" + roleId + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleCreatedAt=" + roleCreatedAt +
                '}';
    }
}

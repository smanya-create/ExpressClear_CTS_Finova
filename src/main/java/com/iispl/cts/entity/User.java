package com.iispl.cts.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String roleId;
    private String employeeId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String status;
    private Timestamp userCreatedAt;

    // Default Constructor
    public User() {
    }

    // Parameterized Constructor
    public User(String userId, String roleId, String employeeId, String username, 
                String password, String fullName, String email, String mobileNumber, 
                String status, Timestamp userCreatedAt) {
        this.userId = userId;
        this.roleId = roleId;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.status = status;
        this.userCreatedAt = userCreatedAt;
    }

    // Getters and Setters
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getUserCreatedAt() {
        return userCreatedAt;
    }

    public void setUserCreatedAt(Timestamp userCreatedAt) {
        this.userCreatedAt = userCreatedAt;
    }

    @Override
    public String toString() {
        return "User {" +
                "userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", status='" + status + '\'' +
                ", userCreatedAt=" + userCreatedAt +
                '}';
    }
}
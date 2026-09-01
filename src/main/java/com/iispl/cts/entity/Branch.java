package com.iispl.cts.entity;

import java.time.LocalDateTime;

public class Branch {
	
	private String branchId;
    private String bankId;
    private String branchCode;
    private String branchName;
    private String ifscCode;
    private String micrCode;
    private String city;
    private String status;
    private LocalDateTime createdAt;

    public Branch() {
    }

    public Branch(String branchId, String bankId, String branchCode,String branchName, String ifscCode, String micrCode,
                       String city, String status, LocalDateTime createdAt)
    {
				        this.branchId = branchId;
				        this.bankId = bankId;
				        this.branchCode = branchCode;
				        this.branchName = branchName;
				        this.ifscCode = ifscCode;
				        this.micrCode = micrCode;
				        this.city = city;
				        this.status = status;
				        this.createdAt = createdAt;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getMicrCode() {
        return micrCode;
    }

    public void setMicrCode(String micrCode) {
        this.micrCode = micrCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

	@Override
	public String toString() {
		return "Branch [branchId=" + branchId + ", bankId=" + bankId + ", branchCode=" + branchCode + ", branchName="
				+ branchName + ", ifscCode=" + ifscCode + ", micrCode=" + micrCode + ", city=" + city + ", status="
				+ status + ", createdAt=" + createdAt + "]";
	}
    
}

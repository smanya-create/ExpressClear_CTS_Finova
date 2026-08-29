package com.iispl.cts.entity;

public class Bank {
		
	private String bankId;
    private String bankCode;
    private String ifscPrefix;
    private String bankName;
    private String bankType;
    private String status;

    public Bank() {
    }

    public Bank(String bankId, String bankCode, String ifscPrefix,
                String bankName, String bankType, String status) {
        this.bankId = bankId;
        this.bankCode = bankCode;
        this.ifscPrefix = ifscPrefix;
        this.bankName = bankName;
        this.bankType = bankType;
        this.status = status;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getIfscPrefix() {
        return ifscPrefix;
    }

    public void setIfscPrefix(String ifscPrefix) {
        this.ifscPrefix = ifscPrefix;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	@Override
	public String toString() {
		return "Bank [bankId=" + bankId + ", bankCode=" + bankCode + ", ifscPrefix=" + ifscPrefix + ", bankName="
				+ bankName + ", bankType=" + bankType + ", status=" + status + "]";
	}
    
}

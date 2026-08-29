package com.iispl.cts.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MasterAccount {

	private String accountId;
    private String branchId;
    private String accountNumber;
    private String accountHolderName;
    private String accountType;
    private BigDecimal accountBalance;
    private String accountStatus;
    private LocalDateTime accountCreatedAt;

    public MasterAccount() {
    }

    public MasterAccount(String accountId, String branchId,
                         String accountNumber, String accountHolderName,
                         String accountType, BigDecimal accountBalance,
                         String accountStatus,
                         LocalDateTime accountCreatedAt) {
        this.accountId = accountId;
        this.branchId = branchId;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.accountBalance = accountBalance;
        this.accountStatus = accountStatus;
        this.accountCreatedAt = accountCreatedAt;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getAccountCreatedAt() {
        return accountCreatedAt;
    }

    public void setAccountCreatedAt(LocalDateTime accountCreatedAt) {
        this.accountCreatedAt = accountCreatedAt;
    }

	@Override
	public String toString() {
		return "MasterAccount [accountId=" + accountId + ", branchId=" + branchId + ", accountNumber=" + accountNumber
				+ ", accountHolderName=" + accountHolderName + ", accountType=" + accountType + ", accountBalance="
				+ accountBalance + ", accountStatus=" + accountStatus + ", accountCreatedAt=" + accountCreatedAt + "]";
	}
    
}

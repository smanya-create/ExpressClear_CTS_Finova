package com.iispl.cts.entity;

public class MasterCheque {

	private String chequeId;
    private String chequeNumber;
    private String accountId;

    public MasterCheque() {
    }

    public MasterCheque(String chequeId, String chequeNumber, String accountId) {
        this.chequeId = chequeId;
        this.chequeNumber = chequeNumber;
        this.accountId = accountId;
    }

    public String getChequeId() {
        return chequeId;
    }

    public void setChequeId(String chequeId) {
        this.chequeId = chequeId;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

	@Override
	public String toString() {
		return "MasterCheque [chequeId=" + chequeId + ", chequeNumber=" + chequeNumber + ", accountId=" + accountId
				+ "]";
	}
    
}

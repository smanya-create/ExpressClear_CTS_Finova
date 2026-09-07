package com.iispl.cts.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class MasterChequeNew implements Serializable {

    private static final long serialVersionUID = 1L;

    private String accountNumber;
    private String chequeNumber;
    private String sortCode;
    private Timestamp issuedAt;

    public MasterChequeNew() {
    }

    public MasterChequeNew(String accountNumber,
                           String chequeNumber,
                           String sortCode,
                           Timestamp issuedAt) {

        this.accountNumber = accountNumber;
        this.chequeNumber = chequeNumber;
        this.sortCode = sortCode;
        this.issuedAt = issuedAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public Timestamp getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Timestamp issuedAt) {
        this.issuedAt = issuedAt;
    }
}
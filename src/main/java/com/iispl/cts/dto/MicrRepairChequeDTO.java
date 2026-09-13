package com.iispl.cts.dto;

public class MicrRepairChequeDTO {

    // Identification
    private String chequeId;
    private String batchId;
    private String chequeNumber;

    // MICR details
    private String fullMicr;
    private String cityCode;
    private String bankCode;
    private String branchCode;

    // Cheque images
    private String chequeImageFront;
    private String chequeImageBack;

    // Rejection / Checker details
    private String reasonId;
    private String reason;
    private String remarks;

    // Cheque status
    private String chequeStatus;


    // =========================
    // Getters and Setters
    // =========================

    public String getChequeId() {
        return chequeId;
    }

    public void setChequeId(String chequeId) {
        this.chequeId = chequeId;
    }


    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }


    public String getFullMicr() {
        return fullMicr;
    }

    public void setFullMicr(String fullMicr) {
        this.fullMicr = fullMicr;
    }


    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }


    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }


    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }


    public String getChequeImageFront() {
        return chequeImageFront;
    }

    public void setChequeImageFront(String chequeImageFront) {
        this.chequeImageFront = chequeImageFront;
    }


    public String getChequeImageBack() {
        return chequeImageBack;
    }

    public void setChequeImageBack(String chequeImageBack) {
        this.chequeImageBack = chequeImageBack;
    }


    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    public String getChequeStatus() {
        return chequeStatus;
    }

    public void setChequeStatus(String chequeStatus) {
        this.chequeStatus = chequeStatus;
    }
}
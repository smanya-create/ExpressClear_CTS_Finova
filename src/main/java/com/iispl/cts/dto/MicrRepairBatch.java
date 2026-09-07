package com.iispl.cts.dto;

import java.sql.Timestamp;

public class MicrRepairBatch {

    private String batchId;
    private Timestamp scanDate;
    private int totalCheques;
    private int micrErrors;
    private String status;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Timestamp getScanDate() {
        return scanDate;
    }

    public void setScanDate(Timestamp scanDate) {
        this.scanDate = scanDate;
    }

    public int getTotalCheques() {
        return totalCheques;
    }

    public void setTotalCheques(int totalCheques) {
        this.totalCheques = totalCheques;
    }

    public int getMicrErrors() {
        return micrErrors;
    }

    public void setMicrErrors(int micrErrors) {
        this.micrErrors = micrErrors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
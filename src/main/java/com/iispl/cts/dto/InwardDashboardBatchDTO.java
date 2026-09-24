package com.iispl.cts.dto;

import java.math.BigDecimal;

public class InwardDashboardBatchDTO {

    private String batchId;
    private int totalCheques;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String batchStatus;
    private boolean hasPendingMicr = false;

    public InwardDashboardBatchDTO() {}

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotalCheques() {
        return totalCheques;
    }

    public void setTotalCheques(int totalCheques) {
        this.totalCheques = totalCheques;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }

    public boolean isHasPendingMicr() {
        return hasPendingMicr;
    }

    public void setHasPendingMicr(boolean hasPendingMicr) {
        this.hasPendingMicr = hasPendingMicr;
    }
}
package com.iispl.cts.dto;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author Sudhansu Sekhar Panda
 */
public class DataEntryBatchItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String batchId;
    private int totalCheques;
    private int pendingCheques;
    private BigDecimal totalAmount;
    private String batchStatus;

    public DataEntryBatchItemDTO() {
    }

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

    public int getPendingCheques() {
        return pendingCheques;
    }

    public void setPendingCheques(int pendingCheques) {
        this.pendingCheques = pendingCheques;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
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

    // Dynamic UI helper methods for ZUL template expressions
    public String getFormattedAmount() {
        if (totalAmount == null) {
            return "₹ 0.00";
        }
        return String.format("₹ %,.2f", totalAmount);
    }

}
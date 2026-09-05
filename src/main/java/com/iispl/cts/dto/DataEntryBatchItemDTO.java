package com.iispl.cts.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DataEntryBatchItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Fields matching inward_batch directly
    private String batchId;
    private int totalCheques;
    private BigDecimal totalAmount;
    private String batchStatus;

    // The single dynamic metric from inward_cheque
    private int pendingCheques;

    public DataEntryBatchItemDTO() {}

    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public int getTotalCheques() { return totalCheques; }
    public void setTotalCheques(int totalCheques) { this.totalCheques = totalCheques; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }

    public int getPendingCheques() { return pendingCheques; }
    public void setPendingCheques(int pendingCheques) { this.pendingCheques = pendingCheques; }

    // UI Presentation Helpers
    public String getFormattedAmount() {
        if (totalAmount == null) return "0.00";
        return new DecimalFormat("##,##,##0.00").format(totalAmount);
    }

    public String getActionLabel() {
        if (pendingCheques == 0) {
            return "Submit to Checker";
        } else if (pendingCheques < totalCheques) {
            return "Resume (" + (totalCheques - pendingCheques) + "/" + totalCheques + ")";
        } else {
            return "Start Entry";
        }
    }

    public String getActionButtonClass() {
        if (pendingCheques == 0) return "btn-action-submit";
        if (pendingCheques < totalCheques) return "btn-action-resume";
        return "btn-action-start";
    }
}
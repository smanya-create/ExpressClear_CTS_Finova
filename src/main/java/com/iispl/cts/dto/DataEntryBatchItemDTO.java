package com.iispl.cts.dto;

import java.io.Serializable;
import java.math.BigDecimal;

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
        if (pendingCheques == 0) {
            return "btn-action-submit";
        } else if (pendingCheques < totalCheques) {
            return "btn-action-resume";
        } else {
            return "btn-action-start";
        }
    }
    
    // Computed Operational Status for Maker UI
    public String getDisplayStatus() {
        if (totalCheques == 0) {
            return "EMPTY";
        }

        // Priority 1: Check if sent back by Checker
        if (batchStatus != null && (
                batchStatus.equalsIgnoreCase("SEND_BACK_TO_MAKER_DATA_ENTRY")
                || batchStatus.equalsIgnoreCase("SEND_BACK_TO_MAKER")
                || batchStatus.equalsIgnoreCase("SEND_BACK_TO_MAKER_MICR"))) {
            return "SENT BACK";
        }

        // Priority 2: Progression counts
        if (pendingCheques == 0) {
            return "RESOLVED";
        }
        if (pendingCheques == totalCheques) {
            return "PENDING";
        }
        return "IN PROGRESS";
    }

    // Styling badge for the computed status
    public String getStatusBadgeStyle() {
        switch (getDisplayStatus()) {
            case "SENT BACK":
                return "background-color: #fff7ed; color: #c2410c; padding: 3px 10px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block; border: 1px solid #fdba74;";
            case "PENDING":
                return "background-color: #fef3c7; color: #d97706; padding: 3px 10px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block; border: 1px solid #fde68a;";
            case "IN PROGRESS":
                return "background-color: #e0f2fe; color: #0284c7; padding: 3px 10px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block; border: 1px solid #bae6fd;";
            case "RESOLVED":
                return "background-color: #dcfce7; color: #15803d; padding: 3px 10px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block; border: 1px solid #bbf7d0;";
            default:
                return "background-color: #f1f5f9; color: #475569; padding: 3px 10px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block; border: 1px solid #e2e8f0;";
        }
    }
}
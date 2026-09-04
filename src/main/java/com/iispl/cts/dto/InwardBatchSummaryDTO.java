package com.iispl.cts.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class InwardBatchSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String batchId;
    private int totalCheques;
    private int acceptedCheques;
    private int rejectedCheques;
    private int pendingCheques;
    private int correctedCheques;
    private BigDecimal totalBatchAmount;
    private BigDecimal acceptedAmount;
    private BigDecimal rejectedAmount;
    private boolean isReconciled;

    public InwardBatchSummaryDTO() {}

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public int getTotalCheques() { return totalCheques; }
    public void setTotalCheques(int totalCheques) { this.totalCheques = totalCheques; }

    public int getAcceptedCheques() { return acceptedCheques; }
    public void setAcceptedCheques(int acceptedCheques) { this.acceptedCheques = acceptedCheques; }

    public int getRejectedCheques() { return rejectedCheques; }
    public void setRejectedCheques(int rejectedCheques) { this.rejectedCheques = rejectedCheques; }

    public int getPendingCheques() { return pendingCheques; }
    public void setPendingCheques(int pendingCheques) { this.pendingCheques = pendingCheques; }

    public int getCorrectedCheques() { return correctedCheques; }
    public void setCorrectedCheques(int correctedCheques) { this.correctedCheques = correctedCheques; }

    public BigDecimal getTotalBatchAmount() { return totalBatchAmount; }
    public void setTotalBatchAmount(BigDecimal totalBatchAmount) { this.totalBatchAmount = totalBatchAmount; }

    public BigDecimal getAcceptedAmount() { return acceptedAmount; }
    public void setAcceptedAmount(BigDecimal acceptedAmount) { this.acceptedAmount = acceptedAmount; }

    public BigDecimal getRejectedAmount() { return rejectedAmount; }
    public void setRejectedAmount(BigDecimal rejectedAmount) { this.rejectedAmount = rejectedAmount; }

    public boolean isReconciled() { return isReconciled; }
    public void setReconciled(boolean reconciled) { isReconciled = reconciled; }
}
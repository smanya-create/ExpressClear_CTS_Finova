package com.iispl.cts.entity.inward;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class InwardBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inwardBatchId;
    private String batchReferenceId;
    private int actualChequeCount;
    private BigDecimal actualTotalAmount;
    private String batchStatus;
    private String uploadedBy;
    private Timestamp uploadedAt;

    public InwardBatch() {
    }

    public InwardBatch(String inwardBatchId, String batchReferenceId, int actualChequeCount, 
                       BigDecimal actualTotalAmount, String batchStatus, String uploadedBy, 
                       Timestamp uploadedAt) {
        this.inwardBatchId = inwardBatchId;
        this.batchReferenceId = batchReferenceId;
        this.actualChequeCount = actualChequeCount;
        this.actualTotalAmount = actualTotalAmount;
        this.batchStatus = batchStatus;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public String getInwardBatchId() { return inwardBatchId; }
    public void setInwardBatchId(String inwardBatchId) { this.inwardBatchId = inwardBatchId; }

    public String getBatchReferenceId() { return batchReferenceId; }
    public void setBatchReferenceId(String batchReferenceId) { this.batchReferenceId = batchReferenceId; }

    public int getActualChequeCount() { return actualChequeCount; }
    public void setActualChequeCount(int actualChequeCount) { this.actualChequeCount = actualChequeCount; }

    public BigDecimal getActualTotalAmount() { return actualTotalAmount; }
    public void setActualTotalAmount(BigDecimal actualTotalAmount) { this.actualTotalAmount = actualTotalAmount; }

    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}
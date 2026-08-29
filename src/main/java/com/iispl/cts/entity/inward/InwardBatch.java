package com.iispl.cts.entity.inward;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class InwardBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inwardBatchId;        // character varying(10) - 'BAT...'
    private String batchReferenceId;     // character varying(50)
    private Integer actualChequeCount;   // integer
    private BigDecimal actualTotalAmount;// numeric(18, 2)
    private String batchStatus;          // character varying(30) default 'Pending'
    private String uploadedBy;           // character varying(10) -> fk to users(user_id)
    private Timestamp uploadedAt;        // timestamp without time zone

    public InwardBatch() {
    }

    public InwardBatch(String inwardBatchId, String batchReferenceId, Integer actualChequeCount, BigDecimal actualTotalAmount, String batchStatus) {
        this.inwardBatchId = inwardBatchId;
        this.batchReferenceId = batchReferenceId;
        this.actualChequeCount = actualChequeCount;
        this.actualTotalAmount = actualTotalAmount;
        this.batchStatus = batchStatus;
    }

    // Getters and Setters
    public String getInwardBatchId() {
        return inwardBatchId;
    }

    public void setInwardBatchId(String inwardBatchId) {
        this.inwardBatchId = inwardBatchId;
    }

    public String getBatchReferenceId() {
        return batchReferenceId;
    }

    public void setBatchReferenceId(String batchReferenceId) {
        this.batchReferenceId = batchReferenceId;
    }

    public Integer getActualChequeCount() {
        return actualChequeCount;
    }

    public void setActualChequeCount(Integer actualChequeCount) {
        this.actualChequeCount = actualChequeCount;
    }

    public BigDecimal getActualTotalAmount() {
        return actualTotalAmount;
    }

    public void setActualTotalAmount(BigDecimal actualTotalAmount) {
        this.actualTotalAmount = actualTotalAmount;
    }

    public String getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
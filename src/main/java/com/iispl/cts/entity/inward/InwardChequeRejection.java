package com.iispl.cts.entity.inward;

import java.io.Serializable;
import java.sql.Timestamp;

public class InwardChequeRejection implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rejectionId;
    private String inwardChequeId;
    private String rejectedReasonId;
    private String remarks;
    private String rejectedBy;
    private Timestamp rejectedAt;

    public InwardChequeRejection() {
    }

    public InwardChequeRejection(String rejectionId,
                                 String inwardChequeId,
                                 String rejectedReasonId,
                                 String remarks,
                                 String rejectedBy,
                                 Timestamp rejectedAt) {

        this.rejectionId = rejectionId;
        this.inwardChequeId = inwardChequeId;
        this.rejectedReasonId = rejectedReasonId;
        this.remarks = remarks;
        this.rejectedBy = rejectedBy;
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionId() {
        return rejectionId;
    }

    public void setRejectionId(String rejectionId) {
        this.rejectionId = rejectionId;
    }

    public String getInwardChequeId() {
        return inwardChequeId;
    }

    public void setInwardChequeId(String inwardChequeId) {
        this.inwardChequeId = inwardChequeId;
    }

    public String getRejectedReasonId() {
        return rejectedReasonId;
    }

    public void setRejectedReasonId(String rejectedReasonId) {
        this.rejectedReasonId = rejectedReasonId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Timestamp getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Timestamp rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}
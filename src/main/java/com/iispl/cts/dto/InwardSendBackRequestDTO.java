package com.iispl.cts.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class InwardSendBackRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sendBackRequestId;
    private String inwardChequeId;
    private String inwardBatchId;
    private Integer reasonId;
    private String reasonName;       // Joined from send_back_reason
    private String reasonCode;       // Joined from send_back_reason
    private String remarks;
    private String sentBackBy;
    private String requestStatus;
    private Timestamp requestedAt;
    private String resolvedBy;
    private Timestamp resolvedAt;

    public InwardSendBackRequestDTO() {}

    public String getSendBackRequestId() { return sendBackRequestId; }
    public void setSendBackRequestId(String sendBackRequestId) { this.sendBackRequestId = sendBackRequestId; }

    public String getInwardChequeId() { return inwardChequeId; }
    public void setInwardChequeId(String inwardChequeId) { this.inwardChequeId = inwardChequeId; }

    public String getInwardBatchId() { return inwardBatchId; }
    public void setInwardBatchId(String inwardBatchId) { this.inwardBatchId = inwardBatchId; }

    public Integer getReasonId() { return reasonId; }
    public void setReasonId(Integer reasonId) { this.reasonId = reasonId; }

    public String getReasonName() { return reasonName; }
    public void setReasonName(String reasonName) { this.reasonName = reasonName; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getSentBackBy() { return sentBackBy; }
    public void setSentBackBy(String sentBackBy) { this.sentBackBy = sentBackBy; }

    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}
package com.iispl.cts.entity.outward;

import java.io.Serializable;

public class RejectedReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rejectedReasonId;
    private String rejectedReasonCode;
    private String rejectedReasonName;
    private String rejectedReasonDescription;

    public RejectedReason() {
    }

    public String getRejectedReasonId() {
        return rejectedReasonId;
    }

    public void setRejectedReasonId(String rejectedReasonId) {
        this.rejectedReasonId = rejectedReasonId;
    }

    public String getRejectedReasonCode() {
        return rejectedReasonCode;
    }

    public void setRejectedReasonCode(String rejectedReasonCode) {
        this.rejectedReasonCode = rejectedReasonCode;
    }

    public String getRejectedReasonName() {
        return rejectedReasonName;
    }

    public void setRejectedReasonName(String rejectedReasonName) {
        this.rejectedReasonName = rejectedReasonName;
    }

    public String getRejectedReasonDescription() {
        return rejectedReasonDescription;
    }

    public void setRejectedReasonDescription(String rejectedReasonDescription) {
        this.rejectedReasonDescription = rejectedReasonDescription;
    }
}
package com.iispl.cts.entity.outward;


import java.io.Serializable;

public class SendBackReason implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reasonId;
    private String reasonCode;
    private String reasonName;
    private String reasonDescription;

    public SendBackReason() {
    }

    public SendBackReason(String reasonId, String reasonCode, String reasonName, String reasonDescription) {
        this.reasonId = reasonId;
        this.reasonCode = reasonCode;
        this.reasonName = reasonName;
        this.reasonDescription = reasonDescription;
    }

    public String getReasonId() { return reasonId; }
    public void setReasonId(String reasonId) { this.reasonId = reasonId; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getReasonName() { return reasonName; }
    public void setReasonName(String reasonName) { this.reasonName = reasonName; }

    public String getReasonDescription() { return reasonDescription; }
    public void setReasonDescription(String reasonDescription) { this.reasonDescription = reasonDescription; }
}
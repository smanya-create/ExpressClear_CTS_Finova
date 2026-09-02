package com.iispl.cts.entity;

import java.io.Serializable;

public class SendBackReason implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reasonId;
    private String reasonCode;
    private String reasonName;
    private String reasonDescription;

    public SendBackReason() {}

    public SendBackReason(int reasonId, String reasonCode, String reasonName, String reasonDescription) {
        this.reasonId = reasonId;
        this.reasonCode = reasonCode;
        this.reasonName = reasonName;
        this.reasonDescription = reasonDescription;
    }

    public int getReasonId() { return reasonId; }
    public void setReasonId(int reasonId) { this.reasonId = reasonId; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getReasonName() { return reasonName; }
    public void setReasonName(String reasonName) { this.reasonName = reasonName; }

    public String getReasonDescription() { return reasonDescription; }
    public void setReasonDescription(String reasonDescription) { this.reasonDescription = reasonDescription; }
}
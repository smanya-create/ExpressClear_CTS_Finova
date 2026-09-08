package com.iispl.cts.dto;

import java.io.Serializable;

public class InwardDashboardBatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String batchId;
    private String batchDate;
    private String source;
    private int totalCheques;
    private int acceptedCheques;
    private int backToMakerCheques;
    private int returnRequestCheques;
    private String displayStatus;

    public InwardDashboardBatchDTO() {}

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getBatchDate() { return batchDate; }
    public void setBatchDate(String batchDate) { this.batchDate = batchDate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public int getTotalCheques() { return totalCheques; }
    public void setTotalCheques(int totalCheques) { this.totalCheques = totalCheques; }

    public int getAcceptedCheques() { return acceptedCheques; }
    public void setAcceptedCheques(int acceptedCheques) { this.acceptedCheques = acceptedCheques; }

    public int getBackToMakerCheques() { return backToMakerCheques; }
    public void setBackToMakerCheques(int backToMakerCheques) { this.backToMakerCheques = backToMakerCheques; }

    public int getReturnRequestCheques() { return returnRequestCheques; }
    public void setReturnRequestCheques(int returnRequestCheques) { this.returnRequestCheques = returnRequestCheques; }

    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }

    public String getStatusBadgeStyle() {
        if ("Partially Processed".equalsIgnoreCase(displayStatus)) {
            return "background-color: #fef3c7; color: #d97706; padding: 4px 14px; border-radius: 12px; font-weight: 700; font-size: 11px; border: 1px solid #fde68a; display: inline-block;";
        } else if ("Completed".equalsIgnoreCase(displayStatus)) {
            return "background-color: #dcfce7; color: #15803d; padding: 4px 14px; border-radius: 12px; font-weight: 700; font-size: 11px; border: 1px solid #bbf7d0; display: inline-block;";
        } else {
            return "background-color: #e0f2fe; color: #0284c7; padding: 4px 14px; border-radius: 12px; font-weight: 700; font-size: 11px; border: 1px solid #bae6fd; display: inline-block;";
        }
    }
}
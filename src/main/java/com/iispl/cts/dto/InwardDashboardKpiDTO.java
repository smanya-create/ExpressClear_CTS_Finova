package com.iispl.cts.dto;

import java.io.Serializable;

public class InwardDashboardKpiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int partiallyProcessedBatches;
    private int sentBackBatches;
    private int returnRequestCheques;

    public int getPartiallyProcessedBatches() { return partiallyProcessedBatches; }
    public void setPartiallyProcessedBatches(int partiallyProcessedBatches) { this.partiallyProcessedBatches = partiallyProcessedBatches; }

    public int getSentBackBatches() { return sentBackBatches; }
    public void setSentBackBatches(int sentBackBatches) { this.sentBackBatches = sentBackBatches; }

    public int getReturnRequestCheques() { return returnRequestCheques; }
    public void setReturnRequestCheques(int returnRequestCheques) { this.returnRequestCheques = returnRequestCheques; }
}
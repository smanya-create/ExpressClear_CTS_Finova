package com.iispl.cts.dto;

public class DashboardSummaryDTO {

	private String batchId;
    private int totalCheques;
    private int rejectionRequestCheques;
    private int makerApprovedCheques;

   
    public String getBatchId()
    { 
    	return batchId; 
    }
    public void setBatchId(String batchId) 
    {
    	this.batchId = batchId;
    }

    public int getTotalCheques() 
    {
    	return totalCheques; 
    }
    public void setTotalCheques(int totalCheques) 
    {
    	this.totalCheques = totalCheques;
    }

    public int getRejectionRequestCheques() 
    { 
    	return rejectionRequestCheques; 
    }
    public void setRejectionRequestCheques(int rejectionRequestCheques) 
    {
    	this.rejectionRequestCheques = rejectionRequestCheques;
    }

    public int getMakerApprovedCheques() 
    { 
    	return makerApprovedCheques;
    }
    public void setMakerApprovedCheques(int makerApprovedCheques)
    { 
    	this.makerApprovedCheques = makerApprovedCheques; 
    }
}

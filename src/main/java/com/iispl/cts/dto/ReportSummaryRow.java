package com.iispl.cts.dto;

public class ReportSummaryRow {
	
	 private String batchId;

     private int totalCheques;

     private int approvedCheques;

     private int rejectedCheques;


     public String getBatchId() {
         return batchId;
     }

     public void setBatchId(String batchId2) {
         this.batchId = batchId2;
     }


     public int getTotalCheques() {
         return totalCheques;
     }

     public void setTotalCheques(int totalCheques) {
         this.totalCheques = totalCheques;
     }


     public int getApprovedCheques() {
         return approvedCheques;
     }

     public void setApprovedCheques(int approvedCheques) {
         this.approvedCheques = approvedCheques;
     }


     public int getRejectedCheques() {
         return rejectedCheques;
     }

     public void setRejectedCheques(int rejectedCheques) {
         this.rejectedCheques = rejectedCheques;
     }
 
}

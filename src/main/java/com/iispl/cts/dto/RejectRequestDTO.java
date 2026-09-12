package com.iispl.cts.dto;

public class RejectRequestDTO {
	
	 private String requestId;
	    private String chequeId;
	    private String batchId;

	    // Rejection request details
	    private String rejectedReasonId;
	    private String rejectedReasonCode;
	    private String rejectedReasonName;
	    private String rejectedReasonDescription;

	    private String remarks;
	    private String rejectedBy;


	    public String getRequestId() {
	        return requestId;
	    }

	    public void setRequestId(String requestId) {
	        this.requestId = requestId;
	    }


	    public String getChequeId() {
	        return chequeId;
	    }

	    public void setChequeId(String chequeId) {
	        this.chequeId = chequeId;
	    }


	    public String getBatchId() {
	        return batchId;
	    }

	    public void setBatchId(String batchId) {
	        this.batchId = batchId;
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

	    public void setRejectedReasonDescription(
	            String rejectedReasonDescription) {

	        this.rejectedReasonDescription =
	                rejectedReasonDescription;
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

}

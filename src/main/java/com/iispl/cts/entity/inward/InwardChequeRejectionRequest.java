package com.iispl.cts.entity.inward;

import java.util.Date;

public class InwardChequeRejectionRequest {

	private Long requestId;
	private String inwardChequeId;
	private String inwardBatchId;
	private String rejectedReasonId;
	private String remarks;
	private String requestedBy;
	private String requestStage;
	private String requestStatus;
	private Date requestedAt;

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public String getInwardChequeId() {
		return inwardChequeId;
	}

	public void setInwardChequeId(String inwardChequeId) {
		this.inwardChequeId = inwardChequeId;
	}

	public String getInwardBatchId() {
		return inwardBatchId;
	}

	public void setInwardBatchId(String inwardBatchId) {
		this.inwardBatchId = inwardBatchId;
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

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getRequestStage() {
		return requestStage;
	}

	public void setRequestStage(String requestStage) {
		this.requestStage = requestStage;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public Date getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Date requestedAt) {
		this.requestedAt = requestedAt;
	}
}
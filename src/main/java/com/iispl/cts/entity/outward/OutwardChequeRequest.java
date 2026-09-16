package com.iispl.cts.entity.outward;

import java.io.Serializable;

public class OutwardChequeRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String requestId;
	private String chequeId;
	private String batchId;
	private String remarks;
	private String reasonId;
	private String reason;

	public OutwardChequeRequest() {
	}

	public OutwardChequeRequest(String requestId, String chequeId, String batchId, String remarks, String reasonId,
			String reason) {
		this.requestId = requestId;
		this.chequeId = chequeId;
		this.batchId = batchId;
		this.remarks = remarks;
		this.reasonId = reasonId;
		this.reason = reason;
	}

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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getReasonId() {
		return reasonId;
	}

	public void setReasonId(String reasonId) {
		this.reasonId = reasonId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
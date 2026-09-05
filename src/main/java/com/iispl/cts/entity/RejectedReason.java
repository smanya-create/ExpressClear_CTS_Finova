package com.iispl.cts.entity;

public class RejectedReason {

	private String rejectedReasonId;
	private String rejectedReasonCode;
	private String rejectedReasonName;
	private String rejectedReasonDescription;

	public RejectedReason() {
	    }
	

	public RejectedReason(String rejectedReasonId,String rejectedReasonCode,String rejectedReasonName,String rejectedReasonDescription) {
					        this.rejectedReasonId = rejectedReasonId;
					        this.rejectedReasonCode = rejectedReasonCode;
					        this.rejectedReasonName = rejectedReasonName;
					        this.rejectedReasonDescription = rejectedReasonDescription;
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

	@Override
	public String toString() {
		return rejectedReasonCode + " - " + rejectedReasonName;
	}
}

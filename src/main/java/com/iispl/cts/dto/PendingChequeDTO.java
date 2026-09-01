package com.iispl.cts.dto;

import java.io.Serializable;

public class PendingChequeDTO implements Serializable {
	private static final long serialVersionUID = 1L;

    private String batchId;
    private String chequeNo;
    private String direction;      // OUTWARD / INWARD
    private String pausedStage;    // Current status / pipeline step
    private String assignedQueue;  // Target UI Queue
    private String notes;
    
    public PendingChequeDTO() {}

    public PendingChequeDTO(String batchId, String chequeNo, String direction, String pausedStage, String assignedQueue, String notes) {
        this.batchId = batchId;
        this.chequeNo = chequeNo;
        this.direction = direction;
        this.pausedStage = pausedStage;
        this.assignedQueue = assignedQueue;
        this.notes = notes;
    }

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getChequeNo() {
		return chequeNo;
	}

	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getPausedStage() {
		return pausedStage;
	}

	public void setPausedStage(String pausedStage) {
		this.pausedStage = pausedStage;
	}

	public String getAssignedQueue() {
		return assignedQueue;
	}

	public void setAssignedQueue(String assignedQueue) {
		this.assignedQueue = assignedQueue;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
    
    

}

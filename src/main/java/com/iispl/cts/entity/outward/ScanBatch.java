package com.iispl.cts.entity.outward;


import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class ScanBatch implements Serializable {

	private static final long serialVersionUID = 1L;

	private String scannedBatchId;
	private String batchReferenceId;
	private int actualChequeCount;
	private BigDecimal actualTotalAmount;
	private String stagingStatus;
	private String batchStatus;
	private String uploadedBy;
	private Timestamp uploadedAt;

	public ScanBatch() {
	}

	public ScanBatch(String scannedBatchId, String batchReferenceId, int actualChequeCount, 
			BigDecimal actualTotalAmount, String stagingStatus, String batchStatus, 
			String uploadedBy, Timestamp uploadedAt) {
		this.scannedBatchId = scannedBatchId;
		this.batchReferenceId = batchReferenceId;
		this.actualChequeCount = actualChequeCount;
		this.actualTotalAmount = actualTotalAmount;
		this.stagingStatus = stagingStatus;
		this.batchStatus = batchStatus;
		this.uploadedBy = uploadedBy;
		this.uploadedAt = uploadedAt;
	}

	public String getScannedBatchId() { 
		return scannedBatchId; 
	}
	public void setScannedBatchId(String scannedBatchId) {
		this.scannedBatchId = scannedBatchId; 
	}

	public String getBatchReferenceId() { 
		return batchReferenceId; 
	}
	public void setBatchReferenceId(String batchReferenceId) { 
		this.batchReferenceId = batchReferenceId; 
	}

	public int getActualChequeCount() { 
		return actualChequeCount;
	}
	public void setActualChequeCount(int actualChequeCount) { 
		this.actualChequeCount = actualChequeCount; 
	}

	public BigDecimal getActualTotalAmount() {
		return actualTotalAmount;
	}
	public void setActualTotalAmount(BigDecimal actualTotalAmount) { 
		this.actualTotalAmount = actualTotalAmount; 
	}

	public String getStagingStatus() {
		return stagingStatus;
	}
	public void setStagingStatus(String stagingStatus) {
		this.stagingStatus = stagingStatus;
	}

	public String getBatchStatus() { 
		return batchStatus;
	}
	public void setBatchStatus(String batchStatus) { 
		this.batchStatus = batchStatus;
	}

	public String getUploadedBy() { 
		return uploadedBy;
	}
	public void setUploadedBy(String uploadedBy) { 
		this.uploadedBy = uploadedBy;
	}

	public Timestamp getUploadedAt() {
		return uploadedAt; 
	}
	public void setUploadedAt(Timestamp uploadedAt) { 
		this.uploadedAt = uploadedAt; 
	}
}

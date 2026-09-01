package com.iispl.cts.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UnprocessedChequeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long chequeId;
    private Long batchId;
    private String batchNo;
    private String originalSessionName;
    private String chequeNo;
    private String sortCode;
    private BigDecimal amount;
    private String status; // PENDING_REPAIR, PENDING_DATA_ENTRY, PENDING_VERIFICATION, RAW
    private boolean isForcedEodRollover;
    private String sendBackReason;
    private String remarks;
    private LocalDateTime createdDate;

    public UnprocessedChequeDTO() {}

	public Long getChequeId() {
		return chequeId;
	}

	public void setChequeId(Long chequeId) {
		this.chequeId = chequeId;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getOriginalSessionName() {
		return originalSessionName;
	}

	public void setOriginalSessionName(String originalSessionName) {
		this.originalSessionName = originalSessionName;
	}

	public String getChequeNo() {
		return chequeNo;
	}

	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}

	public String getSortCode() {
		return sortCode;
	}

	public void setSortCode(String sortCode) {
		this.sortCode = sortCode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isForcedEodRollover() {
		return isForcedEodRollover;
	}

	public void setForcedEodRollover(boolean isForcedEodRollover) {
		this.isForcedEodRollover = isForcedEodRollover;
	}

	public String getSendBackReason() {
		return sendBackReason;
	}

	public void setSendBackReason(String sendBackReason) {
		this.sendBackReason = sendBackReason;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

   
}
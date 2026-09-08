package com.iispl.cts.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InwardReportChequeDTO {

    private String inwardChequeId;
    private String inwardBatchId;

    private String chequeNumber;
    private String micrCode;

    private String draweeName;
    private String draweeAccountNumber;

    private String payeeName;
    private String payeeAccountNumber;

    private BigDecimal chequeAmount;
    private LocalDateTime chequeDate;

    private String chequeStatus;

    private String rejectionId;
    private String rejectedReasonId;

    private String remarks;
    private String rejectedBy;
    private LocalDateTime rejectedAt;

    private String rejectedReasonCode;
    private String rejectedReasonName;
    private String rejectedReasonDescription;
    
    
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
	public String getChequeNumber() {
		return chequeNumber;
	}
	public void setChequeNumber(String chequeNumber) {
		this.chequeNumber = chequeNumber;
	}
	public String getMicrCode() {
		return micrCode;
	}
	public void setMicrCode(String micrCode) {
		this.micrCode = micrCode;
	}
	public String getDraweeName() {
		return draweeName;
	}
	public void setDraweeName(String draweeName) {
		this.draweeName = draweeName;
	}
	public String getDraweeAccountNumber() {
		return draweeAccountNumber;
	}
	public void setDraweeAccountNumber(String draweeAccountNumber) {
		this.draweeAccountNumber = draweeAccountNumber;
	}
	public String getPayeeName() {
		return payeeName;
	}
	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}
	public String getPayeeAccountNumber() {
		return payeeAccountNumber;
	}
	public void setPayeeAccountNumber(String payeeAccountNumber) {
		this.payeeAccountNumber = payeeAccountNumber;
	}
	public BigDecimal getChequeAmount() {
		return chequeAmount;
	}
	public void setChequeAmount(BigDecimal chequeAmount) {
		this.chequeAmount = chequeAmount;
	}
	public LocalDateTime getChequeDate() {
		return chequeDate;
	}
	public void setChequeDate(LocalDateTime chequeDate) {
		this.chequeDate = chequeDate;
	}
	public String getChequeStatus() {
		return chequeStatus;
	}
	public void setChequeStatus(String chequeStatus) {
		this.chequeStatus = chequeStatus;
	}
	public String getRejectionId() {
		return rejectionId;
	}
	public void setRejectionId(String rejectionId) {
		this.rejectionId = rejectionId;
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
	public String getRejectedBy() {
		return rejectedBy;
	}
	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}
	public LocalDateTime getRejectedAt() {
		return rejectedAt;
	}
	public void setRejectedAt(LocalDateTime rejectedAt) {
		this.rejectedAt = rejectedAt;
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
	
}

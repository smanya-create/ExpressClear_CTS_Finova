package com.iispl.cts.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class InwardChequeRejectionDTO {
	
	 private String rejectionId;

	    private String inwardChequeId;

	    private String rejectedReasonId;

	    private String remarks;

	    private String rejectedBy;

	    private Timestamp rejectedAt;


	    // =========================
	    // Rejected Reason Details
	    // =========================

	    private String rejectedReasonCode;

	    private String rejectedReasonName;

	    private String rejectedReasonDescription;


	    // =========================
	    // Cheque Details
	    // =========================

	    private String inwardBatchId;

	    private String chequeNumber;

	    private String micrCode;

	    private String draweeName;

	    private String draweeAccountNumber;

	    private String payeeName;

	    private String payeeAccountNumber;

	    private BigDecimal chequeAmount;

	    private Date chequeDate;

	    private String chequeStatus;


	    // =========================
	    // Getters and Setters
	    // =========================

	    public String getRejectionId() {
	        return rejectionId;
	    }

	    public void setRejectionId(String rejectionId) {
	        this.rejectionId = rejectionId;
	    }


	    public String getInwardChequeId() {
	        return inwardChequeId;
	    }

	    public void setInwardChequeId(String inwardChequeId) {
	        this.inwardChequeId = inwardChequeId;
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


	    public Timestamp getRejectedAt() {
	        return rejectedAt;
	    }

	    public void setRejectedAt(Timestamp rejectedAt) {
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

	    public void setRejectedReasonDescription(
	            String rejectedReasonDescription) {

	        this.rejectedReasonDescription =
	                rejectedReasonDescription;
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

	    public void setDraweeAccountNumber(
	            String draweeAccountNumber) {

	        this.draweeAccountNumber =
	                draweeAccountNumber;
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

	    public void setPayeeAccountNumber(
	            String payeeAccountNumber) {

	        this.payeeAccountNumber =
	                payeeAccountNumber;
	    }


	    public BigDecimal getChequeAmount() {
	        return chequeAmount;
	    }

	    public void setChequeAmount(BigDecimal chequeAmount) {
	        this.chequeAmount = chequeAmount;
	    }


	    public Date getChequeDate() {
	        return chequeDate;
	    }

	    public void setChequeDate(Date chequeDate) {
	        this.chequeDate = chequeDate;
	    }


	    public String getChequeStatus() {
	        return chequeStatus;
	    }

	    public void setChequeStatus(String chequeStatus) {
	        this.chequeStatus = chequeStatus;
	    }
}

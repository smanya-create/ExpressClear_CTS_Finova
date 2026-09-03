package com.iispl.cts.entity.outward;



import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class ScanCheque implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scannedChequeId;
    private String scannedBatchId;
    private String chequeNumber;
    private String micrCode;
    private String draweeName;
    private String draweeAccountNumber;
    private String payeeName;
    private String payeeAccountNumber;
    private BigDecimal chequeAmount;
    private Date chequeDate;
    private String chequeStatus;
    private String accountId;
    private Timestamp createdAt;
    private String cityCode;
    private String bankCode;
    private String branchCode;
    private String chequeImageFront;
    private String chequeImageBack;

    public ScanCheque() {
    }

	

	public ScanCheque(String scannedChequeId, String scannedBatchId, String chequeNumber, String micrCode,
			String draweeName, String draweeAccountNumber, String payeeName, String payeeAccountNumber,
			BigDecimal chequeAmount, Date chequeDate, String chequeStatus, String accountId, Timestamp createdAt,
			String cityCode, String bankCode, String branchCode, String chequeImageFront, String chequeImageBack) {
		super();
		this.scannedChequeId = scannedChequeId;
		this.scannedBatchId = scannedBatchId;
		this.chequeNumber = chequeNumber;
		this.micrCode = micrCode;
		this.draweeName = draweeName;
		this.draweeAccountNumber = draweeAccountNumber;
		this.payeeName = payeeName;
		this.payeeAccountNumber = payeeAccountNumber;
		this.chequeAmount = chequeAmount;
		this.chequeDate = chequeDate;
		this.chequeStatus = chequeStatus;
		this.accountId = accountId;
		this.createdAt = createdAt;
		this.cityCode = cityCode;
		this.bankCode = bankCode;
		this.branchCode = branchCode;
		this.chequeImageFront = chequeImageFront;
		this.chequeImageBack = chequeImageBack;
	}



	public String getChequeImageFront() {
		return chequeImageFront;
	}



	public void setChequeImageFront(String chequeImageFront) {
		this.chequeImageFront = chequeImageFront;
	}



	public String getChequeImageBack() {
		return chequeImageBack;
	}



	public void setChequeImageBack(String chequeImageBack) {
		this.chequeImageBack = chequeImageBack;
	}



	public String getCityCode() {
		return cityCode;
	}



	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}



	public String getBankCode() {
		return bankCode;
	}



	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}



	public String getBranchCode() {
		return branchCode;
	}



	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}



	public String getScannedChequeId() {
		return scannedChequeId;
	}

	public void setScannedChequeId(String scannedChequeId) {
		this.scannedChequeId = scannedChequeId;
	}

	public String getScannedBatchId() {
		return scannedBatchId;
	}

	public void setScannedBatchId(String scannedBatchId) {
		this.scannedBatchId = scannedBatchId;
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

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

   
}

package com.iispl.cts.entity.outward;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class OutwardRejectedCheques implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outwardRejectedChequeId;
    private String outwardChequeId;
    private String rejectedBy;
    private Timestamp rejectedDate;
    private String remarks;
    private String outwardBatchId;       
    private BigDecimal chequeAmount; 
    private String rejectedReasonId;
    private String rejectedReasonName;

    public OutwardRejectedCheques() {
    }

    public OutwardRejectedCheques(String outwardRejectedChequeId, String outwardChequeId, 
                                  String rejectedBy, Timestamp rejectedDate, String remarks, String outwardBatchId, BigDecimal chequeAmount, String rejectedReasonId,
                                  String rejectedReasonName) {
        this.outwardRejectedChequeId = outwardRejectedChequeId;
        this.outwardChequeId = outwardChequeId;
        this.rejectedBy = rejectedBy;
        this.rejectedDate = rejectedDate;
        this.remarks = remarks;
        this.outwardBatchId = outwardBatchId;
        this.chequeAmount = chequeAmount;
        this.rejectedReasonId = rejectedReasonId;
        this.rejectedReasonName = rejectedReasonName;

    }

    public String getOutwardBatchId() {
		return outwardBatchId;
	}

	public void setOutwardBatchId(String outwardBatchId) {
		this.outwardBatchId = outwardBatchId;
	}

	public BigDecimal getChequeAmount() {
		return chequeAmount;
	}

	public void setChequeAmount(BigDecimal chequeAmount) {
		this.chequeAmount = chequeAmount;
	}

	public String getOutwardRejectedChequeId() { return outwardRejectedChequeId; }
    public void setOutwardRejectedChequeId(String outwardRejectedChequeId) { this.outwardRejectedChequeId = outwardRejectedChequeId; }

    public String getOutwardChequeId() { return outwardChequeId; }
    public void setOutwardChequeId(String outwardChequeId) { this.outwardChequeId = outwardChequeId; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public Timestamp getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(Timestamp rejectedDate) { this.rejectedDate = rejectedDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    public String getRejectedReasonId() {
        return rejectedReasonId;
    }

    public void setRejectedReasonId(String rejectedReasonId) {
        this.rejectedReasonId = rejectedReasonId;
    }

    public String getRejectedReasonName() {
        return rejectedReasonName;
    }

    public void setRejectedReasonName(String rejectedReasonName) {
        this.rejectedReasonName = rejectedReasonName;
    }
}

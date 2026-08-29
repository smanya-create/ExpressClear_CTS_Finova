package com.iispl.cts.entity.inward;


import java.io.Serializable;
import java.sql.Timestamp;

public class RRF implements Serializable {

    private static final long serialVersionUID = 1L;

    private String rrfId;
    private String inwardChequeId;
    private String rejectedBy;
    private Timestamp rejectedDate;
    private String remarks;

    public RRF() {
    }

    public RRF(String rrfId, String inwardChequeId, String rejectedBy, Timestamp rejectedDate, String remarks) {
        this.rrfId = rrfId;
        this.inwardChequeId = inwardChequeId;
        this.rejectedBy = rejectedBy;
        this.rejectedDate = rejectedDate;
        this.remarks = remarks;
    }

	public String getRrfId() {
		return rrfId;
	}

	public void setRrfId(String rrfId) {
		this.rrfId = rrfId;
	}

	public String getInwardChequeId() {
		return inwardChequeId;
	}

	public void setInwardChequeId(String inwardChequeId) {
		this.inwardChequeId = inwardChequeId;
	}

	public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}

	public Timestamp getRejectedDate() {
		return rejectedDate;
	}

	public void setRejectedDate(Timestamp rejectedDate) {
		this.rejectedDate = rejectedDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}
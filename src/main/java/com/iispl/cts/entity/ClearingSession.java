package com.iispl.cts.entity;


import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class ClearingSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private Date clearingDate;
    private String sessionStatus;
    private String openedBy;
    private Timestamp openedAt;
    private String closedBy;
    private Timestamp closedAt;
    private String remarks;

    public ClearingSession() {
    }

    public ClearingSession(String sessionId, Date clearingDate, String sessionStatus, String openedBy, 
                           Timestamp openedAt, String closedBy, Timestamp closedAt, String remarks) {
        this.sessionId = sessionId;
        this.clearingDate = clearingDate;
        this.sessionStatus = sessionStatus;
        this.openedBy = openedBy;
        this.openedAt = openedAt;
        this.closedBy = closedBy;
        this.closedAt = closedAt;
        this.remarks = remarks;
    }

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Date getClearingDate() {
		return clearingDate;
	}

	public void setClearingDate(Date clearingDate) {
		this.clearingDate = clearingDate;
	}

	public String getSessionStatus() {
		return sessionStatus;
	}

	public void setSessionStatus(String sessionStatus) {
		this.sessionStatus = sessionStatus;
	}

	public String getOpenedBy() {
		return openedBy;
	}

	public void setOpenedBy(String openedBy) {
		this.openedBy = openedBy;
	}

	public Timestamp getOpenedAt() {
		return openedAt;
	}

	public void setOpenedAt(Timestamp openedAt) {
		this.openedAt = openedAt;
	}

	public String getClosedBy() {
		return closedBy;
	}

	public void setClosedBy(String closedBy) {
		this.closedBy = closedBy;
	}

	public Timestamp getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Timestamp closedAt) {
		this.closedAt = closedAt;
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

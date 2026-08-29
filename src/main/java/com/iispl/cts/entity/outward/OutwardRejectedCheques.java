package com.iispl.cts.entity.outward;

import java.io.Serializable;
import java.sql.Timestamp;

public class OutwardRejectedCheques implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outwardRejectedChequeId;
    private String outwardChequeId;
    private String rejectedBy;
    private Timestamp rejectedDate;
    private String remarks;

    public OutwardRejectedCheques() {
    }

    public OutwardRejectedCheques(String outwardRejectedChequeId, String outwardChequeId, 
                                  String rejectedBy, Timestamp rejectedDate, String remarks) {
        this.outwardRejectedChequeId = outwardRejectedChequeId;
        this.outwardChequeId = outwardChequeId;
        this.rejectedBy = rejectedBy;
        this.rejectedDate = rejectedDate;
        this.remarks = remarks;
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
}

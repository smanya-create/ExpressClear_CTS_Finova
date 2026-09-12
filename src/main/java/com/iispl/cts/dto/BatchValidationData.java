
package com.iispl.cts.dto;

import java.math.BigDecimal;
import java.util.List;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public class BatchValidationData {

    private ScanBatch batch;

    private List<ScanCheque> chequeList;

    private Integer expectedTotalCheques;

    private BigDecimal expectedTotalAmount;

    public BatchValidationData() {
    }

    public BatchValidationData(
            ScanBatch batch,
            List<ScanCheque> chequeList,
            Integer expectedTotalCheques,
            BigDecimal expectedTotalAmount) {

        this.batch = batch;
        this.chequeList = chequeList;
        this.expectedTotalCheques = expectedTotalCheques;
        this.expectedTotalAmount = expectedTotalAmount;
    }

    public ScanBatch getBatch() {
        return batch;
    }

    public void setBatch(ScanBatch batch) {
        this.batch = batch;
    }

    public List<ScanCheque> getChequeList() {
        return chequeList;
    }

    public void setChequeList(List<ScanCheque> chequeList) {
        this.chequeList = chequeList;
    }

    public Integer getExpectedTotalCheques() {
        return expectedTotalCheques;
    }

    public void setExpectedTotalCheques(Integer expectedTotalCheques) {
        this.expectedTotalCheques = expectedTotalCheques;
    }

    public BigDecimal getExpectedTotalAmount() {
        return expectedTotalAmount;
    }

    public void setExpectedTotalAmount(BigDecimal expectedTotalAmount) {
        this.expectedTotalAmount = expectedTotalAmount;
    }
}

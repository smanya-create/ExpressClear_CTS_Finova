package com.iispl.cts.dao.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;
public interface OutwardChequeDAO {

    List<OutwardCheque> getChequesByBatchId(String outwardBatchId);

    int getTotalChequeCountByBatchId(String outwardBatchId);

    BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId);
    
    void transferChequeFromScanToOutwrd(
            Connection connection,
            String scannedBatchId);

}
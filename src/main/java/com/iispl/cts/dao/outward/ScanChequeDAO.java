package com.iispl.cts.dao.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public interface ScanChequeDAO {

    /**
     * Saves the scanned batch information into the database.
     *
     * @param scanBatch batch information parsed from XML
     * @return scanned batch ID
     */
    String saveBatch(Connection connection,List<ScanCheque> chequeList );
    List<ScanCheque> getChequesByBatchId(
            String scannedBatchId);

}
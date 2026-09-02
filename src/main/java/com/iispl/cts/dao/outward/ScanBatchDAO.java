package com.iispl.cts.dao.outward;

import java.sql.Connection;

import com.iispl.cts.entity.outward.ScanBatch;

public interface ScanBatchDAO {

    /**
     * Saves the scanned batch information into the database.
     *
     * @param scanBatch batch information parsed from XML
     * @return scanned batch ID
     */
    String saveBatch(Connection connection,ScanBatch scanBatch);
    ScanBatch getBatchById(
            String scannedBatchId);
    
    void updateBatchStatus(
            Connection connection,
            String batchId,
            String status);

}
package com.iispl.cts.dao.outward;

import java.util.List;
import java.sql.Connection;

import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchDAO {

	List<OutwardBatch> getVerifiedBatches();

	List<OutwardBatch> getRecentBatches();

	List<OutwardBatch> searchBatches(String batchId, String status);

	OutwardBatch getBatchById(String outwardBatchId);


	String transferBatchFromScanToOutward(Connection connection, String scannedBatchId);

	List<OutwardBatch> getPendingBatches();


	List<OutwardBatch> getBatchesReadyForDataEntry();
}
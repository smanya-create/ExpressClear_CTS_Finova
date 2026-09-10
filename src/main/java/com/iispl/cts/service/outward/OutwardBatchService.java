
package com.iispl.cts.service.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchService {

	List<OutwardBatch> getVerifiedBatches();

	List<OutwardBatch> getRecentBatches();

	List<OutwardBatch> searchBatches(String batchId, String status);

	OutwardBatch getBatchById(String outwardBatchId);

	List<OutwardBatch> getPendingBatches(int pageNumber, int pageSize);

	int getPendingBatchCount();

	List<OutwardBatch> getBatchesReadyForDataEntry();

	String getScannedBatchIdByOutwardBatchId(String outwardBatchId);

	String getOutwardBatchIdByScannedBatchId(String scannedBatchId);

	boolean updateBatchStatus(String outwardBatchId, String batchStatus);

	boolean updateBatchStatus(Connection connection, String outwardBatchId, String batchStatus);

	String createOutwardBatchFromScan(Connection connection, String scannedBatchId);
	
	void updateBatchStatus(String batchId, String status);
}

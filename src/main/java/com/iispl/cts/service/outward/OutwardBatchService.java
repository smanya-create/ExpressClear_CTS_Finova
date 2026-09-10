package com.iispl.cts.service.outward;

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
	
	void updateBatchStatus(String batchId, String status);
}

package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.entity.outward.ScanChequeImage;

public interface ScanService {

	/*
	 * ===================================================== Save / update batch and
	 * cheques
	 *
	 * One transaction: Batch + Cheques
	 *
	 * If anything fails: ROLLBACK everything
	 * =====================================================
	 */
	String saveScanBatch(ScanBatch scanBatch, List<ScanCheque> chequeList, List<ScanChequeImage> imageList);

	/*
	 * ===================================================== Retrieve batch by batch
	 * ID =====================================================
	 */
	ScanBatch getBatchById(String scannedBatchId);

	/*
	 * ===================================================== Retrieve all cheques
	 * belonging to batch =====================================================
	 */
	List<ScanCheque> getChequesByBatchId(String scannedBatchId);

	List<ScanBatch> getMakerDashboardBatches();
}
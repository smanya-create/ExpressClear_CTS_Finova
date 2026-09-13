package com.iispl.cts.dao.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.dto.MicrRepairChequeDTO;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public interface ScanChequeDAO {

	String saveBatch(Connection connection, List<ScanCheque> chequeList);

	List<ScanCheque> getChequesByBatchId(String scannedBatchId);

	void updateChequeStatus(Connection connection, String batchId, String status);

	int getDataEnteredCountByBatchId(String scannedBatchId);
}
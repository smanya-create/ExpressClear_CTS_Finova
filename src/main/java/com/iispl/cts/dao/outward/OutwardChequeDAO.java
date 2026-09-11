package com.iispl.cts.dao.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;

public interface OutwardChequeDAO {

	List<OutwardCheque> getChequesByBatchId(String outwardBatchId);

	OutwardCheque getOutwardChequeById(String outwardChequeId);

	OutwardCheque getOutwardChequeByScanCheque(String scannedBatchId, String scannedChequeId);

	OutwardCheque getOutwardChequeByScanCheque(String outwardBatchId, String chequeNumber, String chequeImageFront,
			String chequeImageBack);

	int getTotalChequeCountByBatchId(String outwardBatchId);

	BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId);

	int getDataEnteredCountByBatchId(String outwardBatchId);

	boolean saveDataEntry(OutwardCheque cheque);

	boolean saveDataEntry(Connection connection, OutwardCheque cheque);

	String createOutwardChequeFromScan(Connection connection, String outwardBatchId, OutwardCheque cheque);

	boolean updateChequeStatus(String outwardChequeId, String chequeStatus);

	List<OutwardCheque> getOutwardMicrRepairCheques(String outwardBatchId);

	void saveOutwardMicrRepair(OutwardCheque cheque);

	List<OutwardCheque> getOnHoldCheques(String outwardBatchId);
}
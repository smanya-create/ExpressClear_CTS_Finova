package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.dto.MicrRepairChequeDTO;
import com.iispl.cts.entity.outward.RejectedReason;

public interface OutwardMakerDAO {

	// =========================================================
	// SCAN MICR REPAIR
	// =========================================================

	List<MicrRepairBatch> getScanMicrRepairBatches();

	List<MicrRepairChequeDTO> getScanMicrRepairCheques(String scannedBatchId);

	void saveScanMicrRepair(MicrRepairChequeDTO cheque);

	void submitScanMicrRepair(List<MicrRepairChequeDTO> cheques);

	// =========================================================
	// OUTWARD MICR REPAIR
	// =========================================================

	List<MicrRepairBatch> getOutwardMicrRepairBatches();

	List<MicrRepairChequeDTO> getOutwardMicrRepairCheques(String outwardBatchId);

	void saveOutwardMicrRepair(MicrRepairChequeDTO cheque);

	void submitOutwardMicrRepair(List<MicrRepairChequeDTO> cheques);

	// =========================================================
	// COMMON
	// =========================================================

	List<RejectedReason> getRejectedReasons();

	boolean existsChequeNumberAndAccount(String chequeNumber, String accountNumber);
}
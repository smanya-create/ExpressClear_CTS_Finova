package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.dto.MicrRepairChequeDTO;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public interface OutwardMakerService {

	// =========================================================
	// Scan MICR Repair
	// =========================================================

	List<MicrRepairBatch> getScanMicrRepairBatches();

	List<MicrRepairChequeDTO> getScanMicrRepairCheques(String scannedBatchId);

	void submitScanMicrRepair(List<MicrRepairChequeDTO> cheques);

	void saveScanMicrRepair(MicrRepairChequeDTO cheque);

	// =========================================================
	// Outward MICR Repair
	// =========================================================

	List<MicrRepairBatch> getOutwardMicrRepairBatches();

	List<MicrRepairChequeDTO> getOutwardMicrRepairCheques(String outwardBatchId);

	void submitOutwardMicrRepair(List<MicrRepairChequeDTO> cheques);

	void saveOutwardMicrRepair(MicrRepairChequeDTO cheque);

	List<RejectedReason> getRejectedReasons();

	boolean existsChequeNumberAndAccount(String chequeNumber, String accountNumber);
	
	ScanBatch getMakerBatch(String batchId);

	List<ScanCheque> getMakerBatchCheques(String batchId);

}
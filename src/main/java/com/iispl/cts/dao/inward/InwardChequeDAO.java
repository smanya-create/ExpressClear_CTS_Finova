package com.iispl.cts.dao.inward;

import java.util.List;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;

import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeDAO {

	List<InwardCheque> getMicrRepairRequiredCheques();

	InwardCheque findById(String inwardChequeId);

	List<InwardCheque> findByBatchAndStatus(String batchId, String status);

	List<InwardCheque> getAllCheques();

	InwardCheque getChequeById(String inwardChequeId);

	List<InwardCheque> getChequesByBatchId(String inwardBatchId);

	boolean saveCheque(InwardCheque inwardCheque);

	boolean updateCheque(InwardCheque inwardCheque);

	boolean updateMicrRepair(String inwardChequeId, String inwardBatchId, String originalMicr, String correctedMicrCode,
			String chequeStatus, String repairedBy, String remarks);

	boolean deleteCheque(String inwardChequeId);

	boolean saveRejection(String inwardChequeId, String rejectedReasonId, String remarks, String rejectedBy);
	
	boolean saveRejectionRequest(InwardChequeRejectionRequest request);

	boolean updateChequeStatus(String inwardChequeId, String chequeStatus);

	CbsValidationResult validateCbs(InwardCheque cheque);

}
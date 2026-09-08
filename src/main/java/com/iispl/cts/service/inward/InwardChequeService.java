package com.iispl.cts.service.inward;

import java.util.List;

import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;

public interface InwardChequeService {

	List<InwardCheque> getMicrRepairRequiredCheques();

	InwardCheque findById(String inwardChequeId);

	boolean updateMicrRepair(String inwardChequeId, String inwardBatchId, String originalMicr, String correctedMicrCode,
			String chequeStatus, String repairedBy, String remarks);

	List<InwardCheque> getChequesByBatchAndStatus(String batchId, String status);

	boolean updateChequeDetails(InwardCheque cheque);

	InwardChequeImage getFrontImage(String inwardChequeId);

	InwardChequeImage getBackImage(String inwardChequeId);

	boolean saveRejection(String inwardChequeId, String rejectedReasonId, String remarks, String rejectedBy);

	CbsValidationResult validateCbs(InwardCheque cheque);

}

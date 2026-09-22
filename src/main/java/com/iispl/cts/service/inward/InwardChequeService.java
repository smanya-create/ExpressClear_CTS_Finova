package com.iispl.cts.service.inward;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;

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

	boolean saveRejectionRequest(InwardChequeRejectionRequest request);

	boolean updateChequeStatus(String inwardChequeId, String chequeStatus);
	
	boolean submitMicrRepairBatchToDataEntry(String inwardBatchId);

	CbsValidationResult validateCbs(InwardCheque cheque);

	BigDecimal getAccountBalance(String accountNumber);
	
	String findAccountIdByAccountNumber(String accountNumber);

	String getBankNameByCode(String bankCode);

	String getRejectedReasonDetails(String inwardChequeId);

	String getMakerRejectionRequestDetails(String inwardChequeId);

	boolean saveSendBackRequest(String inwardChequeId, String inwardBatchId, String reasonId, String remarks,
			String sentBackBy);
	
	
	
	// Added for data entry controller requirement
	boolean isReworkRejectionRequest(String inwardChequeId, String inwardBatchId);
	boolean isDataEntryRejectionRequest(String inwardChequeId);
	Map<String, String> getMakerRejectionAlertDetails(String inwardChequeId);
	boolean saveRejectionRequest(String inwardChequeId, String inwardBatchId, String reasonId, String remarks, String requestedBy, String requestStage);

}

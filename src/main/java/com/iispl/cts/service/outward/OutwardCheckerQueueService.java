package com.iispl.cts.service.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.SendBackReason;

public interface OutwardCheckerQueueService {

	// ============================================================
	// CHEQUES
	// ============================================================

	List<OutwardCheque> getChequesByBatchId(String batchId) throws SQLException;

	// ============================================================
	// BATCH STATUS
	// ============================================================

	String getBatchStatus(String batchId) throws SQLException;

	// ============================================================
	// IMAGES
	// ============================================================

	List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId) throws Exception;

	// ============================================================
	// CHEQUE ACTIONS
	// ============================================================

	void verifyCheque(String chequeNo) throws SQLException;

	void returnChequeToMaker(String chequeNo) throws SQLException;

	void rejectCheque(String chequeNo) throws SQLException;

	// ============================================================
	// SEND BACK REASONS
	// ============================================================

	List<SendBackReason> getSendBackReasons() throws SQLException;

	// ============================================================
	// ACCOUNT VALIDATION
	// ============================================================

	boolean isPayeeAccountExists(String accountNumber) throws SQLException;

	// ============================================================
	// UPDATE BATCH STATUS
	// ============================================================

	void updateBatchStatus(String batchId, String status) throws SQLException;
	
	void updateChequeStatus(String chequeNo, String status) throws SQLException;
}
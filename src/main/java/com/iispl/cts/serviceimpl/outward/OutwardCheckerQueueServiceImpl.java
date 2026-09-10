package com.iispl.cts.serviceimpl.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardCheckerQueueDAO;
import com.iispl.cts.daoimpl.outward.OutwardCheckerQueueDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.SendBackReason;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;

public class OutwardCheckerQueueServiceImpl implements OutwardCheckerQueueService {

	private OutwardCheckerQueueDAO outwardCheckerQueueDAO;

	public OutwardCheckerQueueServiceImpl() {

		outwardCheckerQueueDAO = new OutwardCheckerQueueDAOImpl();
	}

	// ============================================================
	// GET CHEQUES BY BATCH
	// ============================================================

	@Override
	public List<OutwardCheque> getChequesByBatchId(String batchId) throws SQLException {

		return outwardCheckerQueueDAO.getChequesByBatchId(batchId);
	}

	// ============================================================
	// GET BATCH STATUS
	// ============================================================

	@Override
	public String getBatchStatus(String batchId) throws SQLException {

		return outwardCheckerQueueDAO.getBatchStatus(batchId);
	}

	// ============================================================
	// VERIFY CHEQUE
	// ============================================================

	@Override
	public void verifyCheque(String chequeNo) throws SQLException {

		outwardCheckerQueueDAO.updateChequeStatus(chequeNo, "VERIFIED");
	}

	// ============================================================
	// RETURN TO MAKER
	// ============================================================

	@Override
	public void returnChequeToMaker(String chequeNo) throws SQLException {

		outwardCheckerQueueDAO.updateChequeStatus(chequeNo, "RETURN_TO_MAKER");
	}

	// ============================================================
	// REJECT CHEQUE
	// ============================================================

	@Override
	public void rejectCheque(String chequeNo) throws SQLException {

		outwardCheckerQueueDAO.updateChequeStatus(chequeNo, "REJECTED");
	}

	// ============================================================
	// GET IMAGES
	// ============================================================

	@Override
	public List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId) throws Exception {

		return outwardCheckerQueueDAO.getImagesByChequeId(outwardChequeId);
	}

	// ============================================================
	// GET SEND BACK REASONS
	// ============================================================

	@Override
	public List<SendBackReason> getSendBackReasons() throws SQLException {

		return outwardCheckerQueueDAO.getSendBackReasons();
	}

	// ============================================================
	// CHECK PAYEE ACCOUNT
	// ============================================================

	@Override
	public boolean isPayeeAccountExists(String accountNumber) throws SQLException {

		return outwardCheckerQueueDAO.isPayeeAccountExists(accountNumber);
	}

	// ============================================================
	// UPDATE BATCH STATUS
	// ============================================================

	@Override
	public void updateBatchStatus(String batchId, String status) throws SQLException {

		outwardCheckerQueueDAO.updateBatchStatus(batchId, status);
	}
	
	@Override
	public void updateChequeStatus(String chequeNo, String status) throws SQLException {
	    outwardCheckerQueueDAO.updateChequeStatus(chequeNo, status);
	}
}
package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.dao.outward.OutwardChequeRequestDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.daoimpl.outward.OutwardChequeRequestDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeRequest;
import com.iispl.cts.service.outward.OutwardChequeService;

public class OutwardChequeServiceImpl implements OutwardChequeService {

	private final OutwardChequeDAO outwardChequeDAO;

	private final OutwardBatchDAO outwardBatchDAO;

	private final OutwardChequeRequestDAO outwardChequeRequestDAO;

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";

	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";

	public OutwardChequeServiceImpl() {

		this.outwardChequeDAO = new OutwardChequeDAOImpl();

		this.outwardBatchDAO = new OutwardBatchDAOImpl();

		this.outwardChequeRequestDAO = new OutwardChequeRequestDAOImpl();
	}

	public OutwardChequeServiceImpl(OutwardChequeDAO outwardChequeDAO, OutwardBatchDAO outwardBatchDAO) {

		if (outwardChequeDAO == null) {

			throw new IllegalArgumentException("OutwardChequeDAO cannot be null");
		}

		if (outwardBatchDAO == null) {

			throw new IllegalArgumentException("OutwardBatchDAO cannot be null");
		}

		this.outwardChequeDAO = outwardChequeDAO;

		this.outwardBatchDAO = outwardBatchDAO;

		this.outwardChequeRequestDAO = new OutwardChequeRequestDAOImpl();
	}

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {
		return outwardChequeDAO.getChequesByBatchId(outwardBatchId);
	}

	@Override
	public OutwardCheque getOutwardChequeById(String outwardChequeId) {
		return outwardChequeDAO.getOutwardChequeById(outwardChequeId);
	}

	@Override
	public OutwardCheque getOutwardChequeByScanCheque(String scannedBatchId, String scannedChequeId) {
		return outwardChequeDAO.getOutwardChequeByScanCheque(scannedBatchId, scannedChequeId);
	}

	@Override
	public int getTotalChequeCountByBatchId(String outwardBatchId) {
		return outwardChequeDAO.getTotalChequeCountByBatchId(outwardBatchId);
	}

	@Override
	public List<OutwardCheque> getOnHoldCheques(String outwardBatchId) {
		return outwardChequeDAO.getOnHoldCheques(outwardBatchId);
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {
		return outwardChequeDAO.getTotalChequeAmountByBatchId(outwardBatchId);
	}

	@Override
	public int getDataEnteredCountByBatchId(String outwardBatchId) {
		return outwardChequeDAO.getDataEnteredCountByBatchId(outwardBatchId);
	}

	@Override
	public boolean saveDataEntry(OutwardCheque cheque) {
		return outwardChequeDAO.saveDataEntry(cheque);
	}

	@Override
	public boolean saveDataEntry(Connection connection, OutwardCheque cheque) {
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		return outwardChequeDAO.saveDataEntry(connection, cheque);
	}

	@Override
	public String createOutwardChequeFromScan(Connection connection, String outwardBatchId, OutwardCheque cheque) {
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		return outwardChequeDAO.createOutwardChequeFromScan(connection, outwardBatchId.trim(), cheque);
	}

	@Override
	public boolean updateChequeStatus(String outwardChequeId, String chequeStatus) {
		return outwardChequeDAO.updateChequeStatus(outwardChequeId, chequeStatus);
	}

	@Override
	public OutwardCheque saveMakerCheque(String scannedBatchId, OutwardCheque cheque) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		if (cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {
			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		String batchId = scannedBatchId.trim();
		String currentStatus = cheque.getChequeStatus();

		if (currentStatus != null) {
			currentStatus = currentStatus.trim().toUpperCase();
		}

		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			String outwardBatchId = outwardBatchDAO.createOutwardBatchFromScan(connection, batchId);

			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
				throw new IllegalStateException("Unable to create/find outward batch for scanned batch: " + batchId);
			}

			outwardBatchId = outwardBatchId.trim();

			cheque.setOutwardBatchId(outwardBatchId);

			String chequeId = cheque.getOutwardChequeId().trim();
			cheque.setOutwardChequeId(chequeId);

			String nextStatus;

			if (STATUS_MICR_REJECTED.equals(currentStatus)) {
				nextStatus = STATUS_MICR_REJECTED;
			} else if (STATUS_ON_HOLD.equals(currentStatus)) {
				nextStatus = STATUS_PENDING_VERIFICATION;
			} else if (STATUS_PENDING_DATA_ENTRY.equals(currentStatus)) {
				nextStatus = STATUS_PENDING_VERIFICATION;
			} else if (STATUS_PENDING_VERIFICATION.equals(currentStatus)) {
				nextStatus = STATUS_PENDING_VERIFICATION;
			} else {
				nextStatus = STATUS_PENDING_VERIFICATION;
			}

			cheque.setChequeStatus(nextStatus);

			boolean updated = outwardChequeDAO.saveDataEntry(connection, cheque);

			if (!updated) {
				throw new IllegalStateException("Unable to save outward cheque: " + chequeId);
			}

			connection.commit();

			cheque.setChequeStatus(nextStatus);

			return cheque;

		} catch (Exception exception) {

			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}

			String errorMessage = exception.getMessage();

			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = exception.getClass().getSimpleName();
			}

			throw new RuntimeException(
					"Unable to save Maker cheque for scanned batch: " + batchId + ". Cause: " + errorMessage,
					exception);

		} finally {

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException closeException) {
					closeException.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean saveMakerRejectionRequest(String scannedBatchId, OutwardCheque cheque, String reasonId,
			String reason, String remarks) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		if (cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {
			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		if (cheque.getOutwardBatchId() == null || cheque.getOutwardBatchId().trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		if (reasonId == null || reasonId.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection reason is required");
		}

		if (reason == null || reason.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection reason cannot be empty");
		}

		if (remarks == null || remarks.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection remarks are required");
		}

		Connection connection = null;

		try {

			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			String chequeId = cheque.getOutwardChequeId().trim();
			String batchId = cheque.getOutwardBatchId().trim();

			boolean requestExists = outwardChequeRequestDAO.existsByChequeId(connection, chequeId);

			if (requestExists) {
				connection.rollback();
				return false;
			}

			boolean chequeUpdated = outwardChequeDAO.updateChequeStatus(connection, chequeId, STATUS_REJECTION_REQUEST);

			if (!chequeUpdated) {
				throw new IllegalStateException("Unable to update cheque status for rejection request: " + chequeId);
			}

			OutwardChequeRequest request = new OutwardChequeRequest();

			request.setChequeId(chequeId);
			request.setBatchId(batchId);
			request.setReasonId(reasonId.trim());
			request.setReason(reason.trim());
			request.setRemarks(remarks.trim());

			boolean requestSaved = outwardChequeRequestDAO.saveRequest(connection, request);

			if (!requestSaved) {
				throw new IllegalStateException("Unable to save outward cheque rejection request: " + chequeId);
			}

			connection.commit();

			cheque.setChequeStatus(STATUS_REJECTION_REQUEST);

			return true;

		} catch (Exception exception) {

			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}

			String errorMessage = exception.getMessage();

			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = exception.getClass().getSimpleName();
			}

			throw new RuntimeException("Unable to save Maker rejection request. Cause: " + errorMessage, exception);

		} finally {

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException closeException) {
					closeException.printStackTrace();
				}
			}
		}
	}

	@Override
	public OutwardChequeRequest getRejectionRequestByChequeId(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return null;
		}

		return outwardChequeRequestDAO.getRequestByChequeId(chequeId.trim());
	}

	@Override
	public List<OutwardChequeRequest> getRejectionRequestsByBatchId(String batchId) {

		if (batchId == null || batchId.trim().isEmpty()) {
			return Collections.emptyList();
		}

		return outwardChequeRequestDAO.getRequestsByBatchId(batchId.trim());
	}

	@Override
	public int getCompletedMakerChequeCountByBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			return 0;
		}

		return outwardChequeDAO.getCompletedMakerChequeCountByBatchId(outwardBatchId.trim());
	}

	@Override
	public boolean submitMakerBatchToChecker(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		String batchId = scannedBatchId.trim();
		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			String outwardBatchId = outwardBatchDAO.createOutwardBatchFromScan(connection, batchId);

			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
				throw new IllegalStateException("Unable to create/find outward batch for scanned batch: " + batchId);
			}

			outwardBatchId = outwardBatchId.trim();

			int totalChequeCount = outwardChequeDAO.getTotalChequeCountByBatchId(outwardBatchId);

			int completedChequeCount = outwardChequeDAO.getCompletedMakerChequeCountByBatchId(outwardBatchId);

			if (totalChequeCount <= 0) {
				throw new IllegalStateException("No cheques are available for submission");
			}

			if (completedChequeCount != totalChequeCount) {

				int pendingChequeCount = totalChequeCount - completedChequeCount;

				throw new IllegalStateException("Batch cannot be submitted. " + pendingChequeCount
						+ " cheques are still pending Maker processing.");
			}

			boolean updated = outwardBatchDAO.updateOutWardBatchStatus(connection, outwardBatchId,
					"PENDING_CHECKER_PROCESS");

			if (!updated) {
				throw new IllegalStateException(
						"Unable to update batch status to PENDING_CHECKER_PROCESS: " + outwardBatchId);
			}

			connection.commit();

			return true;

		} catch (Exception exception) {

			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}

			String errorMessage = exception.getMessage();

			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = exception.getClass().getSimpleName();
			}

			throw new RuntimeException(
					"Unable to submit Maker batch to Checker: " + batchId + ". Cause: " + errorMessage, exception);

		} finally {

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException closeException) {
					closeException.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<OutwardCheque> getMakerDataEntryCheques(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			return Collections.emptyList();
		}

		return outwardChequeDAO.getMakerDataEntryCheques(outwardBatchId.trim());
	}
}
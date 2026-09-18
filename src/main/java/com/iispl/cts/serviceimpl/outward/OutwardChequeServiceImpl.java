package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";
	private static final String STATUS_REJECTION_REJECT = "REJECTION_REJECT";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
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

		String batchId = scannedBatchId.trim();
		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			String outwardBatchId = outwardBatchDAO.createOutwardBatchFromScan(connection, batchId);
			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
				throw new IllegalStateException("Unable to resolve outward batch ID: " + batchId);
			}

			outwardBatchId = outwardBatchId.trim();
			cheque.setOutwardBatchId(outwardBatchId);

			// MICR_REJECTED becomes REJECTION_REQUEST in outward_cheque table
			String currentStatus = cheque.getChequeStatus();
			String nextStatus;
			if (STATUS_MICR_REJECTED.equalsIgnoreCase(currentStatus)) {
				nextStatus = STATUS_REJECTION_REQUEST;
			} else {
				nextStatus = STATUS_PENDING_VERIFICATION;
			}
			cheque.setChequeStatus(nextStatus);

			String existingOutwardChequeId = cheque.getOutwardChequeId();
			if (existingOutwardChequeId != null && !existingOutwardChequeId.trim().isEmpty()) {
				existingOutwardChequeId = existingOutwardChequeId.trim();
				cheque.setOutwardChequeId(existingOutwardChequeId);

				boolean updated = outwardChequeDAO.saveDataEntry(connection, cheque);
				if (!updated) {
					throw new IllegalStateException("Unable to update outward cheque: " + existingOutwardChequeId);
				}
			} else {
				String outwardChequeId = outwardChequeDAO.createOutwardChequeFromScan(connection, outwardBatchId,
						cheque);
				if (outwardChequeId == null || outwardChequeId.trim().isEmpty()) {
					throw new IllegalStateException("Unable to create outward cheque for batch: " + outwardBatchId);
				}
				cheque.setOutwardChequeId(outwardChequeId.trim());
			}

			// Batch status remains untouched
			connection.commit();
			return cheque;

		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ignored) {
				}
			}
			String errorMessage = exception.getMessage();
			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = exception.getClass().getSimpleName();
			}
			throw new RuntimeException("Unable to save Maker cheque: " + batchId + ". Cause: " + errorMessage,
					exception);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
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
		if (remarks == null || remarks.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection remarks are required");
		}

		Connection connection = null;

		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);

			String batchId = scannedBatchId.trim();
			String outwardBatchId = outwardBatchDAO.createOutwardBatchFromScan(connection, batchId);
			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
				throw new IllegalStateException("Unable to resolve outward batch ID: " + batchId);
			}

			// Data Entry rejection sets status to REJECTION_REJECT
			cheque.setOutwardBatchId(outwardBatchId);
			cheque.setChequeStatus(STATUS_REJECTION_REJECT);

			String chequeId = cheque.getOutwardChequeId();
			if (chequeId == null || chequeId.trim().isEmpty()) {
				chequeId = outwardChequeDAO.createOutwardChequeFromScan(connection, outwardBatchId, cheque);
				if (chequeId == null || chequeId.trim().isEmpty()) {
					throw new IllegalStateException("Unable to create outward cheque before rejection");
				}
				cheque.setOutwardChequeId(chequeId.trim());
			} else {
				chequeId = chequeId.trim();
				cheque.setOutwardChequeId(chequeId);
				boolean updated = outwardChequeDAO.saveDataEntry(connection, cheque);
				if (!updated) {
					throw new IllegalStateException("Unable to update cheque for rejection: " + chequeId);
				}
			}

			OutwardChequeRequest request = new OutwardChequeRequest();
			request.setChequeId(chequeId);
			request.setBatchId(outwardBatchId);
			request.setRemarks(remarks.trim());

			if (reasonId != null && !reasonId.trim().isEmpty()) {
				request.setReasonId(reasonId.trim());
			}
			if (reason != null && !reason.trim().isEmpty()) {
				request.setReason(reason.trim());
			}

			boolean requestSaved = outwardChequeRequestDAO.saveRequest(connection, request);
			if (!requestSaved) {
				throw new IllegalStateException("Unable to save outward cheque rejection record: " + chequeId);
			}

			// Batch status stays untouched here
			connection.commit();
			cheque.setChequeStatus(STATUS_REJECTION_REJECT);
			return true;

		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ignored) {
				}
			}
			String errorMessage = exception.getMessage();
			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = exception.getClass().getSimpleName();
			}
			throw new RuntimeException("Unable to save Maker rejection: " + errorMessage, exception);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {
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
				throw new IllegalStateException("Unable to resolve outward batch ID: " + batchId);
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

			// ONLY batch status changes to PENDING_CHECKER_PROCESS
			boolean updated = outwardBatchDAO.updateOutWardBatchStatus(connection, outwardBatchId,
					STATUS_PENDING_CHECKER_PROCESS);
			if (!updated) {
				throw new IllegalStateException(
						"Unable to update batch status to PENDING_CHECKER_PROCESS: " + outwardBatchId);
			}

			// Sync scan_batch status
			String updateScanBatchSql = "UPDATE scan_batch SET batch_status = ? WHERE scanned_batch_id = ?";
			try (PreparedStatement psScan = connection.prepareStatement(updateScanBatchSql)) {
				psScan.setString(1, STATUS_PENDING_CHECKER_PROCESS);
				psScan.setString(2, batchId);
				psScan.executeUpdate();
			} catch (Exception ignored) {
			}

			// Cheques do NOT change to checker status; they stay PENDING_VERIFICATION,
			// REJECTION_REQUEST, REJECTION_REJECT
			connection.commit();
			return true;

		} catch (Exception exception) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ignored) {
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
				} catch (SQLException ignored) {
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
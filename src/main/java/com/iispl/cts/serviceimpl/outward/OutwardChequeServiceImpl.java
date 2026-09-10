package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.outward.OutwardChequeService;

public class OutwardChequeServiceImpl implements OutwardChequeService {

	private final OutwardChequeDAO outwardChequeDAO;
	private final OutwardBatchDAO outwardBatchDAO;

	public OutwardChequeServiceImpl() {
		this.outwardChequeDAO = new OutwardChequeDAOImpl();
		this.outwardBatchDAO = new OutwardBatchDAOImpl();
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
	public List<OutwardCheque> getOutwardMicrRepairCheques(String outwardBatchId) {
		return outwardChequeDAO.getOutwardMicrRepairCheques(outwardBatchId);
	}

	@Override
	public void saveOutwardMicrRepair(OutwardCheque cheque) {
		outwardChequeDAO.saveOutwardMicrRepair(cheque);
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
				throw new IllegalStateException("Unable to create/find outward batch for scanned batch: " + batchId);
			}

			outwardBatchId = outwardBatchId.trim();
			cheque.setOutwardBatchId(outwardBatchId);

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

			connection.commit();
			return cheque;

		} catch (Exception exception) {

			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}

			throw new RuntimeException("Unable to save Maker cheque for scanned batch: " + batchId, exception);

		} finally {

			if (connection != null) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException autoCommitException) {
					autoCommitException.printStackTrace();
				}

				try {
					connection.close();
				} catch (SQLException closeException) {
					closeException.printStackTrace();
				}
			}
		}
	}
}
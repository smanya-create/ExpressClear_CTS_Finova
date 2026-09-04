package com.iispl.cts.serviceimpl.outward;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.dao.outward.ScanChequeDAO;
import com.iispl.cts.daoimpl.outward.ScanBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.ScanChequeDAOImpl;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.entity.outward.ScanChequeImage;
import com.iispl.cts.service.outward.ScanService;

public class ScanServiceImpl implements ScanService {

	private ScanBatchDAO scanBatchDAO;
	private ScanChequeDAO scanChequeDAO;

	public ScanServiceImpl() {

		scanBatchDAO = new ScanBatchDAOImpl();

		scanChequeDAO = new ScanChequeDAOImpl();
	}

	/*
	 * ===================================================== SAVE / UPDATE BATCH +
	 * CHEQUES =====================================================
	 */

	@Override
	public String saveScanBatch(ScanBatch scanBatch, List<ScanCheque> chequeList, List<ScanChequeImage> imageList) {

		/*
		 * ------------------------------------------------- Validate input
		 * -------------------------------------------------
		 */

		if (scanBatch == null) {

			throw new IllegalArgumentException("Scan batch cannot be null");
		}

		if (chequeList == null || chequeList.isEmpty()) {

			throw new IllegalArgumentException("Cheque list cannot be null or empty");
		}

		Connection connection = null;

		try {

			/*
			 * ================================================= Get ONE connection
			 * =================================================
			 */

			connection = DBConnection.getConnection();

			/*
			 * ================================================= Start transaction
			 * =================================================
			 */

			connection.setAutoCommit(false);

			/*
			 * ================================================= Save / update batch
			 * =================================================
			 */

			String batchId = scanBatchDAO.saveBatch(connection, scanBatch);

			/*
			 * ================================================= Save / update cheques
			 * =================================================
			 */

			scanChequeDAO.saveBatch(connection, chequeList);

			/*
			 * ================================================= Images
			 *
			 * Not implemented yet. =================================================
			 */

			/*
			 * ================================================= Everything succeeded
			 *
			 * COMMIT =================================================
			 */

			connection.commit();

			System.out.println("Batch and cheque transaction committed successfully.");

			return batchId;

		} catch (Exception e) {

			/*
			 * ================================================= Something failed
			 *
			 * ROLLBACK EVERYTHING =================================================
			 */

			if (connection != null) {

				try {

					connection.rollback();

					System.out.println("Transaction rolled back successfully.");

				} catch (SQLException rollbackException) {

					rollbackException.printStackTrace();
				}
			}

			throw new RuntimeException("Error while saving scan batch and cheques. " + "Transaction rolled back.", e);

		} finally {

			/*
			 * ================================================= Close connection
			 * =================================================
			 */

			if (connection != null) {

				try {

					connection.close();

				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * ===================================================== GET BATCH BY ID
	 * =====================================================
	 */

	@Override
	public ScanBatch getBatchById(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		return scanBatchDAO.getBatchById(scannedBatchId);
	}

	/*
	 * ===================================================== GET CHEQUES BY BATCH ID
	 * =====================================================
	 */

	@Override
	public List<ScanCheque> getChequesByBatchId(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		return scanChequeDAO.getChequesByBatchId(scannedBatchId);
	}

	@Override
	public List<ScanBatch> getMakerDashboardBatches() {
		return scanBatchDAO.getMakerDashboardBatches();
	}

}
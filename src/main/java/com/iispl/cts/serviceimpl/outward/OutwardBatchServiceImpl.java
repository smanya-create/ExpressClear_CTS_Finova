
package com.iispl.cts.serviceimpl.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;

public class OutwardBatchServiceImpl implements OutwardBatchService {

	private final OutwardBatchDAO outwardBatchDAO;

	public OutwardBatchServiceImpl() {
		outwardBatchDAO = new OutwardBatchDAOImpl();
	}

	@Override
	public List<OutwardBatch> getVerifiedBatches() {
		return outwardBatchDAO.getVerifiedBatches();
	}

	@Override
	public List<OutwardBatch> getRecentBatches() {
		return outwardBatchDAO.getRecentBatches();
	}

	@Override
	public List<OutwardBatch> searchBatches(String batchId, String status) {

		return outwardBatchDAO.searchBatches(batchId, status);
	}

	@Override
	public OutwardBatch getBatchById(String outwardBatchId) {
		return outwardBatchDAO.getBatchById(outwardBatchId);
	}

	@Override
	public List<OutwardBatch> getPendingBatches(int pageNumber, int pageSize) {

		return outwardBatchDAO.getPendingBatches(pageNumber, pageSize);
	}

	@Override
	public int getPendingBatchCount() {
		return outwardBatchDAO.getPendingBatchCount();
	}

	@Override
	public List<OutwardBatch> getBatchesReadyForDataEntry() {
		return outwardBatchDAO.getBatchesReadyForDataEntry();
	}

	@Override
	public String getScannedBatchIdByOutwardBatchId(String outwardBatchId) {

		return outwardBatchDAO.getScannedBatchIdByOutwardBatchId(outwardBatchId);
	}

	@Override
	public String getOutwardBatchIdByScannedBatchId(String scannedBatchId) {

		return outwardBatchDAO.getOutwardBatchIdByScannedBatchId(scannedBatchId);
	}

	@Override
	public boolean updateBatchStatus(String outwardBatchId, String batchStatus) {

		return outwardBatchDAO.updateBatchStatus(outwardBatchId, batchStatus);
	}

	@Override
	public boolean updateBatchStatus(Connection connection, String outwardBatchId, String batchStatus) {

		return outwardBatchDAO.updateBatchStatus(connection, outwardBatchId, batchStatus);
	}

	@Override
	public String createOutwardBatchFromScan(Connection connection, String scannedBatchId) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		return outwardBatchDAO.createOutwardBatchFromScan(connection, scannedBatchId.trim());
	}
}

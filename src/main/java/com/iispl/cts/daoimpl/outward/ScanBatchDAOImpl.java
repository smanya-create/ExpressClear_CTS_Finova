package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.entity.outward.ScanBatch;

public class ScanBatchDAOImpl implements ScanBatchDAO {

	@Override
	public String saveBatch(Connection connection, ScanBatch scanBatch) {

		if (scanBatch == null) {
			throw new IllegalArgumentException("Scan batch cannot be null");
		}

		if (connection == null) {
			throw new IllegalArgumentException("Database connection cannot be null");
		}

		if (scanBatch.getScannedBatchId() == null || scanBatch.getScannedBatchId().trim().isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		String selectSql = "SELECT staging_status FROM scan_batch WHERE scanned_batch_id = ?";

		String insertSql = "INSERT INTO scan_batch "
				+ "(scanned_batch_id, batch_reference_id, actual_cheque_count, actual_total_amount, "
				+ "staging_status, batch_status, uploaded_by, uploaded_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		String updateSql = "UPDATE scan_batch SET " + "batch_reference_id = ?, " + "actual_cheque_count = ?, "
				+ "actual_total_amount = ?, " + "staging_status = ?, " + "batch_status = ?, " + "uploaded_by = ?, "
				+ "uploaded_at = ? " + "WHERE scanned_batch_id = ?";

		try {

			try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

				selectStatement.setString(1, scanBatch.getScannedBatchId());

				try (ResultSet resultSet = selectStatement.executeQuery()) {

					if (!resultSet.next()) {

						try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

							insertStatement.setString(1, scanBatch.getScannedBatchId());
							insertStatement.setString(2, scanBatch.getBatchReferenceId());
							insertStatement.setInt(3, scanBatch.getActualChequeCount());
							insertStatement.setBigDecimal(4, scanBatch.getActualTotalAmount());
							insertStatement.setString(5, scanBatch.getStagingStatus());

							String batchStatus = scanBatch.getBatchStatus();

							if (batchStatus == null || batchStatus.trim().isEmpty()) {
								batchStatus = "PENDING_MAKER_PROCESS";
							}

							insertStatement.setString(6, batchStatus);
							insertStatement.setString(7, scanBatch.getUploadedBy());

							if (scanBatch.getUploadedAt() != null) {
								insertStatement.setTimestamp(8, scanBatch.getUploadedAt());
							} else {
								insertStatement.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
							}

							insertStatement.executeUpdate();
						}

						return scanBatch.getScannedBatchId();
					}

					String existingStagingStatus = resultSet.getString("staging_status");

					if (!"RAW".equalsIgnoreCase(existingStagingStatus)) {
						throw new IllegalStateException("Batch " + scanBatch.getScannedBatchId()
								+ " is already validated or processed and cannot be uploaded again.");
					}
				}
			}

			try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

				updateStatement.setString(1, scanBatch.getBatchReferenceId());
				updateStatement.setInt(2, scanBatch.getActualChequeCount());
				updateStatement.setBigDecimal(3, scanBatch.getActualTotalAmount());
				updateStatement.setString(4, scanBatch.getStagingStatus());

				String batchStatus = scanBatch.getBatchStatus();

				if (batchStatus == null || batchStatus.trim().isEmpty()) {
					batchStatus = "PENDING_MAKER_PROCESS";
				}

				updateStatement.setString(5, batchStatus);
				updateStatement.setString(6, scanBatch.getUploadedBy());

				if (scanBatch.getUploadedAt() != null) {
					updateStatement.setTimestamp(7, scanBatch.getUploadedAt());
				} else {
					updateStatement.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
				}

				updateStatement.setString(8, scanBatch.getScannedBatchId());

				updateStatement.executeUpdate();
			}

			return scanBatch.getScannedBatchId();

		} catch (SQLException e) {
			throw new RuntimeException("Error while saving scan batch: " + scanBatch.getScannedBatchId(), e);
		}
	}

	@Override
	public ScanBatch getBatchById(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		String sql = "SELECT " + "scanned_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "staging_status, " + "batch_status, " + "uploaded_by, " + "uploaded_at "
				+ "FROM scan_batch " + "WHERE scanned_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, scannedBatchId);

			try (ResultSet resultSet = statement.executeQuery()) {

				if (!resultSet.next()) {
					return null;
				}

				ScanBatch scanBatch = new ScanBatch();

				scanBatch.setScannedBatchId(resultSet.getString("scanned_batch_id"));

				scanBatch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

				scanBatch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

				scanBatch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

				scanBatch.setStagingStatus(resultSet.getString("staging_status"));

				scanBatch.setBatchStatus(resultSet.getString("batch_status"));

				scanBatch.setUploadedBy(resultSet.getString("uploaded_by"));

				scanBatch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

				return scanBatch;
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error while retrieving scan batch: " + scannedBatchId, e);
		}
	}

	@Override
	public List<ScanBatch> getMakerDashboardBatches() {

		List<ScanBatch> batchList = new ArrayList<>();

		String sql = "SELECT " + "scanned_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "staging_status, " + "batch_status, " + "uploaded_by, " + "uploaded_at "
				+ "FROM scan_batch " + "WHERE UPPER(TRIM(batch_status)) = 'PENDING_MAKER_PROCESS' " + "AND NOT EXISTS ("
				+ "SELECT 1 " + "FROM outward_batch ob " + "WHERE ob.outward_batch_id = scan_batch.scanned_batch_id"
				+ ") " + "ORDER BY uploaded_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {

				ScanBatch scanBatch = new ScanBatch();

				scanBatch.setScannedBatchId(resultSet.getString("scanned_batch_id"));

				scanBatch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

				scanBatch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

				scanBatch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

				scanBatch.setStagingStatus(resultSet.getString("staging_status"));

				scanBatch.setBatchStatus(resultSet.getString("batch_status"));

				scanBatch.setUploadedBy(resultSet.getString("uploaded_by"));

				scanBatch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

				batchList.add(scanBatch);
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch Maker Dashboard batches from scan_batch", exception);
		}

		return batchList;
	}

	@Override
	public void updateBatchStatus(Connection connection, String batchId, String status) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (batchId == null || batchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Batch ID cannot be null or empty");
		}

		if (status == null || status.trim().isEmpty()) {
			throw new IllegalArgumentException("Batch status cannot be null or empty");
		}

		String sql = "UPDATE scan_batch " + "SET staging_status = ? " + "WHERE scanned_batch_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setString(1, status);
			ps.setString(2, batchId);

			int rowsUpdated = ps.executeUpdate();

			if (rowsUpdated == 0) {
				throw new IllegalStateException("Scan batch not found for batch ID: " + batchId);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Failed to update scan batch status for batch ID: " + batchId, e);
		}
	}

	@Override
	public List<MicrRepairBatch> getScanMicrRepairBatches() {

		List<MicrRepairBatch> batchList = new ArrayList<>();

		String sql = "SELECT " + "sb.scanned_batch_id, " + "sb.uploaded_at, " + "sb.actual_cheque_count, "
				+ "sb.batch_status, " + "COUNT(sc.scanned_cheque_id) AS micr_errors " + "FROM scan_batch sb "
				+ "JOIN scan_cheque sc " + "ON sc.scanned_batch_id = sb.scanned_batch_id "
				+ "WHERE sc.cheque_status = 'MICR_REPAIR_REQUIRED' " + "GROUP BY " + "sb.scanned_batch_id, "
				+ "sb.uploaded_at, " + "sb.actual_cheque_count, " + "sb.batch_status " + "ORDER BY sb.uploaded_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {

				MicrRepairBatch batch = new MicrRepairBatch();

				batch.setBatchId(resultSet.getString("scanned_batch_id"));

				batch.setScanDate(resultSet.getTimestamp("uploaded_at"));

				batch.setTotalCheques(resultSet.getInt("actual_cheque_count"));

				batch.setMicrErrors(resultSet.getInt("micr_errors"));

				batch.setStatus(resultSet.getString("batch_status"));

				batchList.add(batch);
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error while retrieving scan MICR repair batches", e);
		}

		return batchList;
	}
}
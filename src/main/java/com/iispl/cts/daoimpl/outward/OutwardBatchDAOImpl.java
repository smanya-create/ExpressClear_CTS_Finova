package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.entity.outward.OutwardBatch;

public class OutwardBatchDAOImpl implements OutwardBatchDAO {

	@Override
	public List<OutwardBatch> searchBatches(String batchId, String status) {

		List<OutwardBatch> batchList = new ArrayList<>();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
				+ "actual_total_amount, batch_status, uploaded_by, uploaded_at " + "FROM outward_batch "
				+ "WHERE 1 = 1 ");

		List<String> parameters = new ArrayList<>();

		if (batchId != null && !batchId.trim().isEmpty()) {

			sql.append("AND (LOWER(outward_batch_id) LIKE LOWER(?) " + "OR LOWER(batch_reference_id) LIKE LOWER(?)) ");

			String searchBatchId = "%" + batchId.trim() + "%";

			parameters.add(searchBatchId);
			parameters.add(searchBatchId);
		}

		if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {

			sql.append("AND LOWER(TRIM(batch_status)) = LOWER(TRIM(?)) ");

			parameters.add(status.trim());
		}

		sql.append("ORDER BY uploaded_at DESC LIMIT 20");

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

			for (int index = 0; index < parameters.size(); index++) {
				preparedStatement.setString(index + 1, parameters.get(index));
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					batchList.add(mapOutwardBatch(resultSet));
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to search outward batches", exception);
		}

		return batchList;
	}

	@Override
	public OutwardBatch getBatchById(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT outward_batch_id, batch_reference_id, " + "actual_cheque_count, actual_total_amount, "
				+ "batch_status, uploaded_by, uploaded_at " + "FROM outward_batch " + "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapOutwardBatch(resultSet);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward batch with ID: " + outwardBatchId, exception);
		}

		return null;
	}

	@Override
	public List<OutwardBatch> getScanBatchesReadyForDataEntry() {

		List<OutwardBatch> batches = new ArrayList<>();

		String sql = "SELECT " + "sb.scanned_batch_id AS outward_batch_id, " + "sb.batch_reference_id, "
				+ "sb.actual_cheque_count, " + "sb.actual_total_amount, " + "sb.batch_status, " + "sb.uploaded_by, "
				+ "sb.uploaded_at " + "FROM scan_batch sb " + "WHERE UPPER(TRIM(sb.batch_status)) = "
				+ "'PENDING_MAKER_PROCESS' " + "AND sb.actual_cheque_count > 0 " + "AND (" + "SELECT COUNT(*) "
				+ "FROM scan_cheque sc " + "WHERE sc.scanned_batch_id = sb.scanned_batch_id"
				+ ") = sb.actual_cheque_count " + "AND NOT EXISTS (" + "SELECT 1 " + "FROM outward_batch ob "
				+ "WHERE ob.outward_batch_id = sb.scanned_batch_id" + ") " + "ORDER BY sb.uploaded_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				batches.add(mapScanBatchAsOutwardBatch(resultSet));
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch scan batches ready for data entry", exception);
		}

		return batches;
	}

	@Override
	public List<OutwardBatch> getBatchesReadyForDataEntry() {

		List<OutwardBatch> batches = new ArrayList<>();

		String sql = "SELECT " + "ob.outward_batch_id, " + "ob.batch_reference_id, " + "ob.actual_cheque_count, "
				+ "ob.actual_total_amount, " + "ob.batch_status, " + "ob.uploaded_by, " + "ob.uploaded_at "
				+ "FROM outward_batch ob " + "WHERE EXISTS (" + "SELECT 1 " + "FROM outward_cheque send_back "
				+ "WHERE send_back.outward_batch_id = ob.outward_batch_id "
				+ "AND UPPER(TRIM(send_back.cheque_status)) = " + "'SEND_BACK_MAKER'" + ") "
				+ "ORDER BY ob.uploaded_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				batches.add(mapOutwardBatch(resultSet));
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward batches ready for data entry", exception);
		}

		return batches;
	}

	@Override
	public String createOutwardBatchFromScan(Connection connection, String scannedBatchId) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		String batchId = scannedBatchId.trim();

		String existingSql = "SELECT outward_batch_id " + "FROM outward_batch " + "WHERE outward_batch_id = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(existingSql)) {

			preparedStatement.setString(1, batchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getString("outward_batch_id");
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to check existing outward batch: " + batchId, exception);
		}

		String insertSql = "INSERT INTO outward_batch (" + "outward_batch_id, " + "batch_reference_id, "
				+ "actual_cheque_count, " + "actual_total_amount, " + "batch_status, " + "uploaded_by" + ") "
				+ "SELECT " + "scanned_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "'PENDING_CHECKER_PROCESS', " + "uploaded_by " + "FROM scan_batch "
				+ "WHERE scanned_batch_id = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {

			preparedStatement.setString(1, batchId);

			int rowsInserted = preparedStatement.executeUpdate();

			if (rowsInserted == 0) {
				throw new IllegalStateException("Scan batch not found: " + batchId);
			}

			return batchId;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to create outward batch from scan batch: " + batchId, exception);
		}
	}

	@Override
	public String getOutwardBatchIdByScannedBatchId(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		String sql = "SELECT ob.outward_batch_id " + "FROM outward_batch ob " + "WHERE ob.outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, scannedBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getString("outward_batch_id");
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find outward batch for scanned batch: " + scannedBatchId, exception);
		}

		return null;
	}

	@Override
	public String getScannedBatchIdByOutwardBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT sb.scanned_batch_id " + "FROM scan_batch sb " + "INNER JOIN outward_batch ob "
				+ "ON sb.scanned_batch_id = ob.outward_batch_id " + "WHERE ob.outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getString("scanned_batch_id");
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find scanned batch for outward batch: " + outwardBatchId, exception);
		}

		return null;
	}

	@Override
	public boolean updateOutWardBatchStatus(String outwardBatchId, String batchStatus) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		if (batchStatus == null || batchStatus.trim().isEmpty()) {

			throw new IllegalArgumentException("Batch status cannot be null or empty");
		}

		String sql = "UPDATE outward_batch " + "SET batch_status = ? " + "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, batchStatus.trim());

			preparedStatement.setString(2, outwardBatchId.trim());

			return preparedStatement.executeUpdate() > 0;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to update outward batch status for: " + outwardBatchId, exception);
		}
	}

	@Override
	public boolean updateOutWardBatchStatus(Connection connection, String outwardBatchId, String batchStatus) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		if (batchStatus == null || batchStatus.trim().isEmpty()) {

			throw new IllegalArgumentException("Batch status cannot be null or empty");
		}

		String sql = "UPDATE outward_batch " + "SET batch_status = ? " + "WHERE outward_batch_id = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, batchStatus.trim());

			preparedStatement.setString(2, outwardBatchId.trim());

			return preparedStatement.executeUpdate() > 0;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to update outward batch status for: " + outwardBatchId, exception);
		}
	}

	@Override
public List<OutwardBatch> getPendingBatches(int pageNumber, int pageSize) {

    List<OutwardBatch> batches = new ArrayList<>();

    if (pageNumber < 1) {
        pageNumber = 1;
    }

    if (pageSize < 1) {
        pageSize = 5;
    }

    String sql = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
            + "actual_total_amount, batch_status, uploaded_by, uploaded_at "
            + "FROM outward_batch "
            + "WHERE UPPER(TRIM(batch_status)) IN (?, ?) "
            + "ORDER BY uploaded_at DESC "
            + "LIMIT ? OFFSET ?";

    try (Connection connection = DBConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

        int offset = (pageNumber - 1) * pageSize;

        preparedStatement.setString(1, "PENDING_CHECKER_PROCESS");
        preparedStatement.setString(2, "ON_HOLD");
        preparedStatement.setInt(3, pageSize);
        preparedStatement.setInt(4, offset);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                batches.add(mapOutwardBatch(resultSet));
            }
        }

    } catch (SQLException exception) {
        throw new RuntimeException("Unable to fetch pending checker batches", exception);
    }

    return batches;
}
	@Override
	public int getPendingBatchCount() {

	    String sql = "SELECT COUNT(*) "
	            + "FROM outward_batch "
	            + "WHERE UPPER(TRIM(batch_status)) IN (?, ?)";

	    try (Connection connection = DBConnection.getConnection();
	            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

	        preparedStatement.setString(1, "PENDING_CHECKER_PROCESS");
	        preparedStatement.setString(2, "ON_HOLD");

	        try (ResultSet resultSet = preparedStatement.executeQuery()) {

	            if (resultSet.next()) {
	                return resultSet.getInt(1);
	            }
	        }

	    } catch (SQLException exception) {
	        throw new RuntimeException(
	                "Unable to count pending checker batches", exception);
	    }

	    return 0;
	}

	@Override
	public List<OutwardBatch> getVerifiedBatches() {

		List<OutwardBatch> batches = new ArrayList<>();

		String sql = "SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch "
				+ "WHERE UPPER(TRIM(batch_status)) = ? " + "ORDER BY uploaded_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, "VERIFIED");

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					batches.add(mapOutwardBatch(resultSet));
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch verified outward batches", exception);
		}

		return batches;
	}

	@Override
	public List<OutwardBatch> getRecentBatches() {

		List<OutwardBatch> batchList = new ArrayList<>();

		String sql = "SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch "
				+ "ORDER BY uploaded_at DESC " + "LIMIT 20";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				batchList.add(mapOutwardBatch(resultSet));
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch recent outward batches", exception);
		}

		return batchList;
	}

	@Override
	public List<MicrRepairBatch> getOutwardMicrRepairBatches() {

		List<MicrRepairBatch> batchList = new ArrayList<>();

		String sql = "SELECT " + "ob.outward_batch_id, " + "ob.uploaded_at, " + "ob.actual_cheque_count, "
				+ "ob.batch_status, " + "COUNT(oc.outward_cheque_id) AS micr_errors " + "FROM outward_batch ob "
				+ "JOIN outward_cheque oc " + "ON oc.outward_batch_id = ob.outward_batch_id "
				+ "WHERE UPPER(TRIM(oc.cheque_status)) = " + "'PENDING_MICR_REPAIR' " + "GROUP BY "
				+ "ob.outward_batch_id, " + "ob.uploaded_at, " + "ob.actual_cheque_count, " + "ob.batch_status "
				+ "ORDER BY ob.uploaded_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {

				MicrRepairBatch batch = new MicrRepairBatch();

				batch.setBatchId(resultSet.getString("outward_batch_id"));

				batch.setScanDate(resultSet.getTimestamp("uploaded_at"));

				batch.setTotalCheques(resultSet.getInt("actual_cheque_count"));

				batch.setMicrErrors(resultSet.getInt("micr_errors"));

				batch.setStatus(resultSet.getString("batch_status"));

				batchList.add(batch);
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward MICR repair batches", exception);
		}

		return batchList;
	}

	private OutwardBatch mapOutwardBatch(ResultSet resultSet) throws SQLException {

		OutwardBatch outwardBatch = new OutwardBatch();

		outwardBatch.setOutwardBatchId(resultSet.getString("outward_batch_id"));

		outwardBatch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

		outwardBatch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

		outwardBatch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

		outwardBatch.setBatchStatus(resultSet.getString("batch_status"));

		outwardBatch.setUploadedBy(resultSet.getString("uploaded_by"));

		outwardBatch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

		return outwardBatch;
	}

	private OutwardBatch mapScanBatchAsOutwardBatch(ResultSet resultSet) throws SQLException {

		OutwardBatch outwardBatch = new OutwardBatch();

		outwardBatch.setOutwardBatchId(resultSet.getString("outward_batch_id"));

		outwardBatch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

		outwardBatch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

		outwardBatch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

		outwardBatch.setBatchStatus(resultSet.getString("batch_status"));

		outwardBatch.setUploadedBy(resultSet.getString("uploaded_by"));

		outwardBatch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

		return outwardBatch;
	}

	@Override
	public void updateBatchStatus(String batchId, String status) {

		String sql = "UPDATE outward_batch " + "SET batch_status = ? " + "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, status);
			preparedStatement.setString(2, batchId);

			preparedStatement.executeUpdate();

		} catch (Exception exception) {
			throw new RuntimeException("Unable to update batch status", exception);
		}
	}

}
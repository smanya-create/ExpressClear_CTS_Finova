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
                + "actual_total_amount, batch_status, uploaded_by, uploaded_at "
                + "FROM outward_batch WHERE 1 = 1 ");

        List<String> parameters = new ArrayList<>();

        if (batchId != null && !batchId.trim().isEmpty()) {
            sql.append("AND (LOWER(outward_batch_id) LIKE LOWER(?) OR LOWER(batch_reference_id) LIKE LOWER(?)) ");
            String searchBatchId = "%" + batchId.trim() + "%";
            parameters.add(searchBatchId);
            parameters.add(searchBatchId);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND LOWER(batch_status) = LOWER(?) ");
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
        } catch (Exception exception) {
            throw new RuntimeException("Unable to search outward batches", exception);
        }

        return batchList;
    }

    @Override
    public OutwardBatch getBatchById(String outwardBatchId) {
        String sql = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
                + "actual_total_amount, batch_status, uploaded_by, uploaded_at "
                + "FROM outward_batch WHERE outward_batch_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, outwardBatchId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOutwardBatch(resultSet);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("Unable to fetch outward batch with ID: " + outwardBatchId, exception);
        }

        return null;
    }

    @Override
    public List<OutwardBatch> getBatchesReadyForDataEntry() {
        List<OutwardBatch> batches = new ArrayList<>();
        String sql = "SELECT ob.outward_batch_id, ob.batch_reference_id, ob.actual_cheque_count, "
                + "ob.actual_total_amount, ob.batch_status, ob.uploaded_by, ob.uploaded_at "
                + "FROM outward_batch ob "
                + "WHERE UPPER(TRIM(ob.batch_status)) = 'PENDING' "
                + "AND EXISTS ( "
                + "    SELECT 1 FROM outward_cheque oc "
                + "    WHERE oc.outward_batch_id = ob.outward_batch_id "
                + "    AND UPPER(TRIM(oc.cheque_status)) = 'PENDING_DATA_ENTRY' "
                + ") ORDER BY ob.uploaded_at ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                batches.add(mapOutwardBatch(resultSet));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Unable to fetch batches ready for data entry", exception);
        }

        return batches;
    }

    @Override
    public String transferBatchFromScanToOutward(Connection connection, String scannedBatchId) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
        }

        String sql = "INSERT INTO outward_batch ("
                + "batch_reference_id, actual_cheque_count, actual_total_amount, batch_status, uploaded_by"
                + ") SELECT batch_reference_id, actual_cheque_count, actual_total_amount, 'PENDING', uploaded_by "
                + "FROM scan_batch WHERE scanned_batch_id = ? "
                + "RETURNING outward_batch_id";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, scannedBatchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Scan batch not found for batch ID: " + scannedBatchId);
                }
                return rs.getString("outward_batch_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to transfer scan batch " + scannedBatchId + " to outward batch", e);
        }
    }

    @Override
    public List<OutwardBatch> getPendingBatches(int pageNumber, int pageSize) {

        List<OutwardBatch> batches = new ArrayList<>();

        String sql = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
                + "actual_total_amount, batch_status, uploaded_by, uploaded_at "
                + "FROM outward_batch "
                + "WHERE UPPER(TRIM(batch_status)) = ? "
                + "ORDER BY uploaded_at DESC "
                + "LIMIT ? OFFSET ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sql)) {

            int offset = (pageNumber - 1) * pageSize;

            prepStmt.setString(1, "PENDING");
            prepStmt.setInt(2, pageSize);
            prepStmt.setInt(3, offset);

            try (ResultSet resultSet = prepStmt.executeQuery()) {

                while (resultSet.next()) {
                    batches.add(mapOutwardBatch(resultSet));
                }
            }

        } catch (Exception exception) {
            throw new RuntimeException("Unable to fetch pending batches", exception);
        }

        return batches;
    }
    @Override
    public int getPendingBatchCount() {

        String sql = "SELECT COUNT(*) "
                + "FROM outward_batch "
                + "WHERE UPPER(TRIM(batch_status)) = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sql)) {

            prepStmt.setString(1, "PENDING");

            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (Exception exception) {
            throw new RuntimeException("Unable to count pending batches", exception);
        }

        return 0;
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
	@Override
	public List<OutwardBatch> getVerifiedBatches() {
		List<OutwardBatch> batches = new ArrayList<>();
		String selectSql = "SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch "
				+ "WHERE batch_status = ?";
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement prepStmt = connection.prepareStatement(selectSql)) {
			prepStmt.setString(1, "VERIFIED");
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next()) {
				batches.add(new OutwardBatch(rs.getString("outward_batch_id"), rs.getString("batch_reference_id"),
						rs.getInt("actual_cheque_count"), rs.getBigDecimal("actual_total_amount"),
						rs.getString("batch_status"), rs.getString("uploaded_by"), rs.getTimestamp("uploaded_at")));
			}

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
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

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch recent outward batches", exception);
		}

		return batchList;
	}

	@Override
	public List<MicrRepairBatch> getOutwardMicrRepairBatches() {

	    List<MicrRepairBatch> batchList =
	            new ArrayList<>();

	    String sql =
	            "SELECT "
	          + "ob.outward_batch_id, "
	          + "ob.uploaded_at, "
	          + "ob.actual_cheque_count, "
	          + "ob.batch_status, "
	          + "COUNT(oc.outward_cheque_id) AS micr_errors "
	          + "FROM outward_batch ob "
	          + "JOIN outward_cheque oc "
	          + "ON oc.outward_batch_id = ob.outward_batch_id "
	          + "WHERE UPPER(TRIM(oc.cheque_status)) = 'PENDING_MICR_REPAIR' "
	          + "GROUP BY "
	          + "ob.outward_batch_id, "
	          + "ob.uploaded_at, "
	          + "ob.actual_cheque_count, "
	          + "ob.batch_status "
	          + "ORDER BY ob.uploaded_at DESC";

	    try (
	            Connection connection =
	                    DBConnection.getConnection();

	            PreparedStatement preparedStatement =
	                    connection.prepareStatement(sql);

	            ResultSet resultSet =
	                    preparedStatement.executeQuery()
	    ) {

	        while (resultSet.next()) {

	            MicrRepairBatch batch =
	                    new MicrRepairBatch();

	            batch.setBatchId(
	                    resultSet.getString(
	                            "outward_batch_id"));

	            batch.setScanDate(
	                    resultSet.getTimestamp(
	                            "uploaded_at"));

	            batch.setTotalCheques(
	                    resultSet.getInt(
	                            "actual_cheque_count"));

	            batch.setMicrErrors(
	                    resultSet.getInt(
	                            "micr_errors"));

	            batch.setStatus(
	                    resultSet.getString(
	                            "batch_status"));

	            batchList.add(batch);
	        }

	    } catch (SQLException e) {

	        throw new RuntimeException(
	                "Error while retrieving outward MICR repair batches",
	                e);
	    }

	    return batchList;
	}

}
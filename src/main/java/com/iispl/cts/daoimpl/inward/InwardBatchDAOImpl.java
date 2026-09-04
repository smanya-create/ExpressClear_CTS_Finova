package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.dao.inward.InwardBatchDAO;
import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.entity.inward.InwardBatch;

public class InwardBatchDAOImpl implements InwardBatchDAO {
	
	// Static in-memory storage (replace with JDBC ResultSet later)
    private final List<InwardBatch> batchTable = new ArrayList<>();

    public InwardBatchDAOImpl() {
        initStaticBatches();
    }

    private void initStaticBatches() {
        batchTable.add(new InwardBatch(
            "BAT1001", 
            "REF-BATCH-2026-001", 
            2, 
            new BigDecimal("3775000.00"), 
            "Processing", 
            "USR1001", 
            Timestamp.valueOf("2026-08-31 15:14:01")
        ));

        batchTable.add(new InwardBatch(
            "BAT1002", 
            "REF-BATCH-2026-002", 
            3, 
            new BigDecimal("697500.00"), 
            "Processing", 
            "USR1001", 
            Timestamp.valueOf("2026-08-31 15:14:01")
        ));
    }

    @Override
    public List<InwardBatch> findAllActiveBatches() {
        return new ArrayList<>(batchTable);
    }

    @Override
    public InwardBatch findById(String batchId) {
        return batchTable.stream()
                .filter(b -> b.getInwardBatchId().equalsIgnoreCase(batchId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean updateStatus(String batchId, String status) {
        InwardBatch batch = findById(batchId);
        if (batch != null) {
            batch.setBatchStatus(status);
            return true;
        }
        return false;
    }
	
	
    
    @Override
    public List<InwardBatch> getAllBatches() {

        List<InwardBatch> batches = new ArrayList<>();

        String sql = "SELECT inward_batch_id, batch_reference_id, "
                + "actual_cheque_count, actual_total_amount, "
                + "batch_status, uploaded_by, uploaded_at "
                + "FROM inward_batch "
                + "ORDER BY uploaded_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                InwardBatch batch = new InwardBatch();

                batch.setInwardBatchId(
                        resultSet.getString("inward_batch_id"));

                batch.setBatchReferenceId(
                        resultSet.getString("batch_reference_id"));

                batch.setActualChequeCount(
                        resultSet.getInt("actual_cheque_count"));

                batch.setActualTotalAmount(
                        resultSet.getBigDecimal("actual_total_amount"));

                batch.setBatchStatus(
                        resultSet.getString("batch_status"));

                batch.setUploadedBy(
                        resultSet.getString("uploaded_by"));

                batch.setUploadedAt(
                        resultSet.getTimestamp("uploaded_at"));

                batches.add(batch);
            }

            return batches;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to load inward batches: " + e.getMessage(), e);
        }
    }

    @Override
    public InwardBatch getBatchById(String inwardBatchId) {

        String sql = "SELECT inward_batch_id, batch_reference_id, "
                + "actual_cheque_count, actual_total_amount, "
                + "batch_status, uploaded_by, uploaded_at "
                + "FROM inward_batch "
                + "WHERE inward_batch_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, inwardBatchId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    InwardBatch batch = new InwardBatch();

                    batch.setInwardBatchId(
                            resultSet.getString("inward_batch_id"));

                    batch.setBatchReferenceId(
                            resultSet.getString("batch_reference_id"));

                    batch.setActualChequeCount(
                            resultSet.getInt("actual_cheque_count"));

                    batch.setActualTotalAmount(
                            resultSet.getBigDecimal("actual_total_amount"));

                    batch.setBatchStatus(
                            resultSet.getString("batch_status"));

                    batch.setUploadedBy(
                            resultSet.getString("uploaded_by"));

                    batch.setUploadedAt(
                            resultSet.getTimestamp("uploaded_at"));

                    return batch;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to get inward batch: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public boolean saveBatch(InwardBatch inwardBatch) {

        String sql = "INSERT INTO inward_batch "
                + "(inward_batch_id, batch_reference_id, "
                + "actual_cheque_count, actual_total_amount, "
                + "batch_status, uploaded_by, uploaded_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, inwardBatch.getInwardBatchId());
            statement.setString(2, inwardBatch.getBatchReferenceId());
            statement.setInt(3, inwardBatch.getActualChequeCount());
            statement.setBigDecimal(4, inwardBatch.getActualTotalAmount());
            statement.setString(5, inwardBatch.getBatchStatus());
            statement.setString(6, inwardBatch.getUploadedBy());
            statement.setTimestamp(7, inwardBatch.getUploadedAt());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to save inward batch: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateBatch(InwardBatch inwardBatch) {

        String sql = "UPDATE inward_batch SET "
                + "batch_reference_id = ?, "
                + "actual_cheque_count = ?, "
                + "actual_total_amount = ?, "
                + "batch_status = ?, "
                + "uploaded_by = ?, "
                + "uploaded_at = ? "
                + "WHERE inward_batch_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, inwardBatch.getBatchReferenceId());
            statement.setInt(2, inwardBatch.getActualChequeCount());
            statement.setBigDecimal(3, inwardBatch.getActualTotalAmount());
            statement.setString(4, inwardBatch.getBatchStatus());
            statement.setString(5, inwardBatch.getUploadedBy());
            statement.setTimestamp(6, inwardBatch.getUploadedAt());
            statement.setString(7, inwardBatch.getInwardBatchId());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to update inward batch: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteBatch(String inwardBatchId) {

        String sql = "DELETE FROM inward_batch "
                + "WHERE inward_batch_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, inwardBatchId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to delete inward batch: " + e.getMessage(), e);
        }
    
}

	@Override
	public List<DashboardSummaryDTO> getDashboardBatches() {

	    String dashboardSummaryQuery =
	            "SELECT ib.inward_batch_id, ib.actual_cheque_count AS total_cheques, "
	          + "COUNT(CASE WHEN ic.cheque_status = 'maker_approved' THEN 1 END) AS normal_cheques, "
	          + "COUNT(CASE WHEN ic.cheque_status = 'rejection_request' THEN 1 END) AS rejected_cheques "
	          + "FROM inward_batch ib "
	          + "LEFT JOIN inward_cheque ic ON ic.inward_batch_id = ib.inward_batch_id "
	          + "WHERE ib.batch_status = 'submit_to_ichecker' "
	          + "GROUP BY ib.inward_batch_id "
	          + "ORDER BY ib.inward_batch_id;";

	    List<DashboardSummaryDTO> batchList = new ArrayList<>();

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(dashboardSummaryQuery);
	         ResultSet rs = ps.executeQuery()) {
	        
	        while (rs.next()) {

	            DashboardSummaryDTO summary = new DashboardSummaryDTO();

	            summary.setBatchId(rs.getString("inward_batch_id"));
	            summary.setTotalCheques(rs.getInt("total_cheques"));
	            summary.setRejectionRequestCheques(rs.getInt("rejected_cheques"));
	            summary.setMakerApprovedCheques(rs.getInt("normal_cheques"));

	            batchList.add(summary);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return batchList;
	}
}


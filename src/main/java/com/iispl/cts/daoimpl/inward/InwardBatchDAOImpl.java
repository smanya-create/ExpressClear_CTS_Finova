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
import com.iispl.cts.dto.InwardReportChequeDTO;
import com.iispl.cts.entity.inward.InwardBatch;

public class InwardBatchDAOImpl implements InwardBatchDAO {

	// Static in-memory storage (replace with JDBC ResultSet later)
//    private final List<InwardBatch> batchTable = new ArrayList<>();

	public InwardBatchDAOImpl() {
//        initStaticBatches();
	}

//    private void initStaticBatches() {
//        batchTable.add(new InwardBatch(
//            "BAT1001", 
//            "REF-BATCH-2026-001", 
//            2, 
//            new BigDecimal("3775000.00"), 
//            "Processing", 
//            "USR1001", 
//            Timestamp.valueOf("2026-08-31 15:14:01")
//        ));
//
//        batchTable.add(new InwardBatch(
//            "BAT1002", 
//            "REF-BATCH-2026-002", 
//            3, 
//            new BigDecimal("697500.00"), 
//            "Processing", 
//            "USR1001", 
//            Timestamp.valueOf("2026-08-31 15:14:01")
//        ));
//    }

//    @Override
//    public List<InwardBatch> findAllActiveBatches() {
//        return new ArrayList<>(batchTable);
//    }

//    @Override
//    public InwardBatch findById(String batchId) {
//        return batchTable.stream()
//                .filter(b -> b.getInwardBatchId().equalsIgnoreCase(batchId))
//                .findFirst()
//                .orElse(null);
//    }

	@Override
	public boolean updateStatus(String batchId, String status) {
		String sql = "UPDATE inward_batch SET batch_status = ? WHERE inward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, status != null ? status.trim() : "");
			statement.setString(2, batchId != null ? batchId.trim() : "");

			int rows = statement.executeUpdate();
			System.out.println("DEBUG: updateStatus updated batch " + batchId + " to " + status + " | Rows affected: " + rows);
			return rows > 0;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to update status for batch " + batchId + ": " + e.getMessage(), e);
		}
	}

	@Override
	public List<InwardBatch> getAllBatches() {

		List<InwardBatch> batches = new ArrayList<>();

		String sql = "SELECT inward_batch_id, batch_reference_id, " + "actual_cheque_count, actual_total_amount, "
				+ "batch_status, uploaded_by, uploaded_at " + "FROM inward_batch " + "ORDER BY uploaded_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

				InwardBatch batch = new InwardBatch();

				batch.setInwardBatchId(resultSet.getString("inward_batch_id"));

				batch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

				batch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

				batch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

				batch.setBatchStatus(resultSet.getString("batch_status"));

				batch.setUploadedBy(resultSet.getString("uploaded_by"));

				batch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

				batches.add(batch);
			}

			return batches;

		} catch (Exception e) {

			e.printStackTrace();

			throw new RuntimeException("Failed to load inward batches: " + e.getMessage(), e);
		}
	}

	@Override
	public InwardBatch getBatchById(String inwardBatchId) {

		String sql = "SELECT inward_batch_id, batch_reference_id, " + "actual_cheque_count, actual_total_amount, "
				+ "batch_status, uploaded_by, uploaded_at " + "FROM inward_batch " + "WHERE inward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardBatchId);

			try (ResultSet resultSet = statement.executeQuery()) {

				if (resultSet.next()) {

					InwardBatch batch = new InwardBatch();

					batch.setInwardBatchId(resultSet.getString("inward_batch_id"));

					batch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

					batch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

					batch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

					batch.setBatchStatus(resultSet.getString("batch_status"));

					batch.setUploadedBy(resultSet.getString("uploaded_by"));

					batch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

					return batch;
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

			throw new RuntimeException("Failed to get inward batch: " + e.getMessage(), e);
		}

		return null;
	}

	@Override
	public boolean saveBatch(InwardBatch inwardBatch) {

		String sql = "INSERT INTO inward_batch " + "(inward_batch_id, batch_reference_id, "
				+ "actual_cheque_count, actual_total_amount, " + "batch_status, uploaded_by, uploaded_at) "
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

			throw new RuntimeException("Failed to save inward batch: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean updateBatch(InwardBatch inwardBatch) {

		String sql = "UPDATE inward_batch SET " + "batch_reference_id = ?, " + "actual_cheque_count = ?, "
				+ "actual_total_amount = ?, " + "batch_status = ?, " + "uploaded_by = ?, " + "uploaded_at = ? "
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

			throw new RuntimeException("Failed to update inward batch: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean deleteBatch(String inwardBatchId) {

		String sql = "DELETE FROM inward_batch " + "WHERE inward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardBatchId);

			return statement.executeUpdate() > 0;

		} catch (Exception e) {

			e.printStackTrace();

			throw new RuntimeException("Failed to delete inward batch: " + e.getMessage(), e);
		}

	}

	@Override
	public List<DashboardSummaryDTO> getDashboardBatches() {
		String dashboardSummaryQuery = "SELECT ib.inward_batch_id, ib.batch_status, ib.actual_cheque_count AS total_cheques, "
		        + "COUNT(CASE WHEN ic.cheque_status = 'maker_approved' THEN 1 END) AS normal_cheques, "
		        + "COUNT(CASE WHEN ic.cheque_status = 'rejection_request' THEN 1 END) AS rejected_cheques "
		        + "FROM inward_batch ib "
		        + "LEFT JOIN inward_cheque ic ON ic.inward_batch_id = ib.inward_batch_id "
		        + "WHERE ib.batch_status IN ('CHECKER_PROCESSING', 'IN_VERIFICATION') "
		        + "GROUP BY ib.inward_batch_id, ib.batch_status, ib.actual_cheque_count "
		        + "ORDER BY ib.inward_batch_id;";

	    List<DashboardSummaryDTO> batchList = new ArrayList<>();

	    try (Connection conn = DBConnection.getConnection();
	            PreparedStatement ps = conn.prepareStatement(dashboardSummaryQuery);
	            ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            DashboardSummaryDTO summary = new DashboardSummaryDTO();
	            summary.setBatchId(rs.getString("inward_batch_id"));
	            summary.setBatchStatus(rs.getString("batch_status"));
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

	@Override
	public List<InwardBatch> getBatchesForMicrRepair() {

	    List<InwardBatch> batches = new ArrayList<>();

	    String sql =
	            "SELECT ib.inward_batch_id, "
	          + "ib.batch_reference_id, "
	          + "ib.actual_cheque_count, "
	          + "ib.actual_total_amount, "
	          + "ib.batch_status, "
	          + "ib.uploaded_by, "
	          + "ib.uploaded_at, "

	          + "(SELECT COUNT(*) "
	          + " FROM inward_cheque ic2 "
	          + " WHERE ic2.inward_batch_id = ib.inward_batch_id "
	          + " AND ic2.cheque_status IN "
	          + " ('MICR_REPAIR_PENDING', "
	          + "  'MICR_REPAIR_IN_PROGRESS', "
	          + "  'SEND_BACK_TO_MAKER')) "
	          + "AS micr_repair_pending_count "

	          + "FROM inward_batch ib "

	          + "WHERE EXISTS ( "
	          + "    SELECT 1 "
	          + "    FROM inward_cheque ic "
	          + "    WHERE ic.inward_batch_id = ib.inward_batch_id "
	          + "    AND ic.cheque_status IN "
	          + "    ('MICR_REPAIR_PENDING', "
	          + "     'MICR_REPAIR_IN_PROGRESS', "
	          + "     'SEND_BACK_TO_MAKER') "
	          + ") "

	          + "ORDER BY ib.uploaded_at DESC";

	    try (Connection connection = DBConnection.getConnection();
	         PreparedStatement statement =
	                 connection.prepareStatement(sql);
	         ResultSet resultSet =
	                 statement.executeQuery()) {

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

	            batch.setMicrRepairPendingCount(
	                    resultSet.getInt("micr_repair_pending_count"));

	            batches.add(batch);
	        }

	        return batches;

	    } catch (Exception e) {

	        e.printStackTrace();

	        throw new RuntimeException(
	                "Failed to load MICR repair batches: "
	                + e.getMessage(),
	                e);
	    }
	}

	public List<InwardReportChequeDTO> getChequesByBatch() {
        List<InwardReportChequeDTO> chequeList = new ArrayList<>();

        String sql = "SELECT "
                + "ic.inward_cheque_id, "
                + "ic.inward_batch_id, "
                + "ic.cheque_number, "
                + "ic.micr_code, "
                + "ic.drawee_name, "
                + "ic.drawee_account_number, "
                + "ic.payee_name, "
                + "ic.payee_account_number, "
                + "ic.cheque_amount, "
                + "ic.cheque_date, "
                + "ic.cheque_status, "
                + "drawee_bank.bank_name AS drawee_bank, "
                + "presenting_bank.bank_name AS presenting_bank, "
                + "r.rejection_id, "
                + "r.rejected_reason_id, "
                + "r.remarks, "
                + "r.rejected_by, "
                + "r.rejected_at, "
                + "rr.rejected_reason_code, "
                + "rr.rejected_reason_name, "
                + "rr.rejected_reason_description "
                + "FROM inward_cheque ic "
                + "INNER JOIN inward_batch ib "
                + "ON ib.inward_batch_id = ic.inward_batch_id "
                + "LEFT JOIN master_account_new drawee_acc "
                + "ON drawee_acc.account_number = ic.drawee_account_number "
                + "LEFT JOIN master_branch drawee_branch "
                + "ON drawee_branch.branch_id = drawee_acc.branch_id "
                + "LEFT JOIN master_bank drawee_bank "
                + "ON drawee_bank.bank_code = drawee_branch.bank_code "
                + "LEFT JOIN master_account_new payee_acc "
                + "ON payee_acc.account_number = ic.payee_account_number "
                + "LEFT JOIN master_branch payee_branch "
                + "ON payee_branch.branch_id = payee_acc.branch_id "
                + "LEFT JOIN master_bank presenting_bank "
                + "ON presenting_bank.bank_code = payee_branch.bank_code "
                + "LEFT JOIN inward_cheque_rejection r "
                + "ON r.inward_cheque_id = ic.inward_cheque_id "
                + "LEFT JOIN rejected_reasons rr "
                + "ON rr.rejected_reason_id = r.rejected_reason_id "
                + "WHERE ib.batch_status = 'COMPLETED' "
                + "ORDER BY ib.inward_batch_id, ic.inward_cheque_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InwardReportChequeDTO dto = new InwardReportChequeDTO();

                dto.setInwardChequeId(rs.getString("inward_cheque_id"));
                dto.setInwardBatchId(rs.getString("inward_batch_id"));
                dto.setChequeNumber(rs.getString("cheque_number"));
                dto.setMicrCode(rs.getString("micr_code"));
                dto.setDraweeName(rs.getString("drawee_name"));
                dto.setDraweeAccountNumber(rs.getString("drawee_account_number"));
                dto.setPayeeName(rs.getString("payee_name"));
                dto.setPayeeAccountNumber(rs.getString("payee_account_number"));
                dto.setChequeAmount(rs.getBigDecimal("cheque_amount"));
                dto.setChequeStatus(rs.getString("cheque_status"));

                dto.setDraweeBank(rs.getString("drawee_bank"));
                dto.setPresentingBank(rs.getString("presenting_bank"));

                Timestamp chequeDate = rs.getTimestamp("cheque_date");
                if (chequeDate != null) {
                    dto.setChequeDate(chequeDate.toLocalDateTime());
                }

                dto.setRejectionId(rs.getString("rejection_id"));
                dto.setRejectedReasonId(rs.getString("rejected_reason_id"));
                dto.setRemarks(rs.getString("remarks"));
                dto.setRejectedBy(rs.getString("rejected_by"));

                Timestamp rejectedAt = rs.getTimestamp("rejected_at");
                if (rejectedAt != null) {
                    dto.setRejectedAt(rejectedAt.toLocalDateTime());
                }

                dto.setRejectedReasonCode(rs.getString("rejected_reason_code"));
                dto.setRejectedReasonName(rs.getString("rejected_reason_name"));
                dto.setRejectedReasonDescription(rs.getString("rejected_reason_description"));

                chequeList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chequeList;
    }


	@Override
	public boolean updateProcessingBatchStatus(String batchId, String status) {
	    String query = "UPDATE inward_batch SET batch_status = ? WHERE inward_batch_id = ?";

	    try (Connection conn = DBConnection.getConnection();
	            PreparedStatement ps = conn.prepareStatement(query)) {
	        ps.setString(1, status);
	        ps.setString(2, batchId);
	        return ps.executeUpdate() > 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return false;
	}
}

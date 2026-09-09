package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardDashboardDAO;
import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.dto.InwardDashboardKpiDTO;
import com.iispl.cts.enums.inward.InwardBatchStatus;
import com.iispl.cts.enums.inward.InwardChequeStatus;

public class InwardDashboardDAOImpl implements InwardDashboardDAO {

    @Override
    public InwardDashboardKpiDTO getKpiMetrics() {
        InwardDashboardKpiDTO kpi = new InwardDashboardKpiDTO();

        // 1. Partially Processed Batches: Inward working batches not yet finalized or sent back
        String sqlPartially = "SELECT COUNT(DISTINCT inward_batch_id) FROM inward_batch " +
                              "WHERE batch_status IN ('" 
                              + InwardBatchStatus.PROCESSING.name() + "', 'Pending', 'DATA_ENTRY_PENDING', 'DATA_ENTRY_IN_PROGRESS', 'VALIDATED')";

        // 2. Sent Back Batches: Count distinct batches having sent-back status OR containing sent-back items
        String sqlSentBack = "SELECT COUNT(DISTINCT b.inward_batch_id) " +
                             "FROM inward_batch b " +
                             "LEFT JOIN inward_cheque c ON b.inward_batch_id = c.inward_batch_id " +
                             "WHERE b.batch_status IN ('" 
                             + InwardBatchStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                             + InwardBatchStatus.SEND_BACK_TO_MAKER_MICR.name() + "', 'SEND_BACK_TO_MAKER') " +
                             "   OR c.cheque_status IN ('" 
                             + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                             + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "', 'SEND_BACK_TO_MAKER')";
        // 3. Return Requests: Cheques where Maker requested rejection or items finalized as returns
        String sqlReturns = "SELECT COUNT(*) FROM inward_cheque " +
                            "WHERE cheque_status IN ('" 
                            + InwardChequeStatus.REJECTION_REQUESTED.name() + "', '" 
                            + InwardChequeStatus.REJECTED.name() + "', '" 
                            + InwardChequeStatus.RRF_PENDING.name() + "', '" 
                            + InwardChequeStatus.RETURNED.name() + "')";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlPartially);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) kpi.setPartiallyProcessedBatches(rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlSentBack);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) kpi.setSentBackBatches(rs.getInt(1));
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlReturns);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) kpi.setReturnRequestCheques(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kpi;
    }

    @Override
    public List<InwardDashboardBatchDTO> getRecentBatches() {
        List<InwardDashboardBatchDTO> batches = new ArrayList<>();
        
        // Accurate aggregation matching current instrument lifecycle enums
        String sql = "SELECT " +
                     "    b.inward_batch_id, " +
                     "    b.batch_status, " +
                     "    b.uploaded_at, " +
                     "    COALESCE(b.actual_cheque_count, 0) AS total_count, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('" 
                     + InwardChequeStatus.CHECKER_PROCESSING_PENDING.name() + "', '" 
                     + InwardChequeStatus.COMPLETED.name() + "', '" 
                     + InwardChequeStatus.CLEARED.name() + "', 'ACCEPTED', 'DATA_ENTRY_COMPLETED') THEN 1 END) AS accepted_count, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER.name() + "') THEN 1 END) AS back_to_maker_count, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('" 
                     + InwardChequeStatus.REJECTION_REQUESTED.name() + "', '" 
                     + InwardChequeStatus.REJECTED.name() + "', '" 
                     + InwardChequeStatus.RRF_PENDING.name() + "', '" 
                     + InwardChequeStatus.RETURNED.name() + "') THEN 1 END) AS return_request_count " +
                     "FROM inward_batch b " +
                     "LEFT JOIN inward_cheque c ON b.inward_batch_id = c.inward_batch_id " +
                     "GROUP BY b.inward_batch_id, b.batch_status, b.uploaded_at, b.actual_cheque_count " +
                     "ORDER BY b.uploaded_at DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InwardDashboardBatchDTO dto = new InwardDashboardBatchDTO();
                dto.setBatchId(rs.getString("inward_batch_id"));

                java.sql.Timestamp ts = rs.getTimestamp("uploaded_at");
                dto.setBatchDate(ts != null ? sdf.format(ts) : "-");

                dto.setSource("CHI");
                dto.setTotalCheques(rs.getInt("total_count"));
                dto.setAcceptedCheques(rs.getInt("accepted_count"));
                dto.setBackToMakerCheques(rs.getInt("back_to_maker_count"));
                dto.setReturnRequestCheques(rs.getInt("return_request_count"));

                String bStatus = rs.getString("batch_status");
                
                // Display Status alignment
                if (dto.getBackToMakerCheques() > 0 
                        || InwardBatchStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(bStatus)
                        || InwardBatchStatus.SEND_BACK_TO_MAKER_MICR.name().equalsIgnoreCase(bStatus)) {
                    dto.setDisplayStatus("Sent Back");
                } else if (InwardBatchStatus.COMPLETED.name().equalsIgnoreCase(bStatus)
                        || InwardBatchStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(bStatus)) {
                    dto.setDisplayStatus("Completed");
                } else {
                    dto.setDisplayStatus("Partially Processed");
                }

                batches.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    @Override
    public String determineNextWorkspace(String batchId) {
        String sql = "SELECT " +
                     "    COUNT(CASE WHEN cheque_status IN ('" 
                     + InwardChequeStatus.MICR_REPAIR_PENDING.name() + "', '" 
                     + InwardChequeStatus.MICR_REPAIR_IN_PROGRESS.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "', 'MICR_REPAIR_REQUIRED') THEN 1 END) AS micr_count, " +
                     "    COUNT(CASE WHEN cheque_status IN ('" 
                     + InwardChequeStatus.DATA_ENTRY_PENDING.name() + "', '" 
                     + InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER.name() + "') THEN 1 END) AS de_count " +
                     "FROM inward_cheque " +
                     "WHERE inward_batch_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("micr_count") > 0) {
                        return "/inward/maker/micr-repair/micr-repair.zul";
                    }
                    if (rs.getInt("de_count") > 0) {
                        return "/inward/maker/data-entry/data-entry.zul";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "/inward/maker/data-entry/data-entry.zul";
    }
}
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

        // 1. Partially Processed Batches: Maker batches not sent back, not completed, needing work
        String sqlPartially = 
            "SELECT COUNT(DISTINCT b.inward_batch_id) FROM inward_batch b " +
            "WHERE b.batch_status NOT IN ('COMPLETED', 'REJECTED') " +
            "  AND b.batch_status != 'CHECKER_PROCESSING_PENDING' " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM inward_cheque sc WHERE sc.inward_batch_id = b.inward_batch_id " +
            "      AND sc.cheque_status IN ('" 
            + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
            + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "', 'SEND_BACK_TO_MAKER') " +
            "  ) " +
            "  AND EXISTS ( " +
            "      SELECT 1 FROM inward_cheque ic WHERE ic.inward_batch_id = b.inward_batch_id " +
            "      AND ic.cheque_status IN ('" 
            + InwardChequeStatus.DATA_ENTRY_PENDING.name() + "', '" 
            + InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name() + "', '" 
            + InwardChequeStatus.MICR_REPAIR_PENDING.name() + "', '" 
            + InwardChequeStatus.MICR_REPAIR_IN_PROGRESS.name() + "') " +
            "  )";

        // 2. Sent Back Batches: ONLY batches that currently have active sent-back cheques needing Maker work
        String sqlSentBack = 
            "SELECT COUNT(DISTINCT b.inward_batch_id) FROM inward_batch b " +
            "WHERE b.batch_status NOT IN ('COMPLETED', 'REJECTED') " +
            "  AND EXISTS ( " +
            "      SELECT 1 FROM inward_cheque sc WHERE sc.inward_batch_id = b.inward_batch_id " +
            "      AND sc.cheque_status IN ('" 
            + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
            + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "', 'SEND_BACK_TO_MAKER') " +
            "  )";

        // 3. Return Requests: ONLY cheques requested for rejection in Maker-actionable batches
        String sqlReturns = 
            "SELECT COUNT(*) FROM inward_cheque c " +
            "JOIN (" +
            "    SELECT b.inward_batch_id " +
            "    FROM inward_batch b " +
            "    LEFT JOIN inward_cheque sc ON b.inward_batch_id = sc.inward_batch_id " +
            "    WHERE b.batch_status NOT IN ('COMPLETED', 'REJECTED') " +
            "    GROUP BY b.inward_batch_id, b.batch_status " +
            "    HAVING COUNT(CASE WHEN sc.cheque_status IN ('SEND_BACK_TO_MAKER_DATA_ENTRY', 'SEND_BACK_TO_MAKER_MICR', 'SEND_BACK_TO_MAKER') THEN 1 END) > 0 " +
            "        OR (b.batch_status NOT IN ('CHECKER_PROCESSING_PENDING', 'SEND_BACK_TO_MAKER_DATA_ENTRY', 'SEND_BACK_TO_MAKER_MICR', 'SEND_BACK_TO_MAKER') " +
            "            AND COUNT(CASE WHEN sc.cheque_status IN ('DATA_ENTRY_PENDING', 'DATA_ENTRY_IN_PROGRESS', 'MICR_REPAIR_PENDING', 'MICR_REPAIR_IN_PROGRESS') THEN 1 END) > 0) " +
            ") maker_batches ON c.inward_batch_id = maker_batches.inward_batch_id " +
            "WHERE c.cheque_status = '" + InwardChequeStatus.REJECTION_REQUESTED.name() + "'";
        
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

        String sql = 
            "SELECT * FROM ( " +
            "    SELECT " +
            "        b.inward_batch_id, " +
            "        b.batch_status, " +
            "        b.uploaded_at, " +
            "        COALESCE(b.actual_cheque_count, 0) AS total_count, " +
            "        COUNT(CASE WHEN c.cheque_status IN ('CHECKER_PROCESSING_PENDING', 'COMPLETED', 'CLEARED', 'ACCEPTED', 'DATA_ENTRY_COMPLETED', 'MAKER_RETURNED') THEN 1 END) AS accepted_count, " +
            "        COUNT(CASE WHEN c.cheque_status IN ('SEND_BACK_TO_MAKER_DATA_ENTRY', 'SEND_BACK_TO_MAKER_MICR', 'SEND_BACK_TO_MAKER') THEN 1 END) AS back_to_maker_count, " +
            "        COUNT(CASE WHEN c.cheque_status = 'REJECTION_REQUESTED' THEN 1 END) AS return_request_count, " +
            "        COUNT(CASE WHEN c.cheque_status IN ('DATA_ENTRY_PENDING', 'DATA_ENTRY_IN_PROGRESS', 'MICR_REPAIR_PENDING', 'MICR_REPAIR_IN_PROGRESS') THEN 1 END) AS maker_work_count " +
            "    FROM inward_batch b " +
            "    LEFT JOIN inward_cheque c ON b.inward_batch_id = c.inward_batch_id " +
            "    WHERE b.batch_status NOT IN ('COMPLETED', 'REJECTED') " +
            "    GROUP BY b.inward_batch_id, b.batch_status, b.uploaded_at, b.actual_cheque_count " +
            ") sub " +
            "WHERE sub.back_to_maker_count > 0 " +
            "   OR (sub.batch_status NOT IN ('CHECKER_PROCESSING_PENDING', 'SEND_BACK_TO_MAKER_DATA_ENTRY', 'SEND_BACK_TO_MAKER_MICR', 'SEND_BACK_TO_MAKER') AND sub.maker_work_count > 0) " +
            "ORDER BY sub.uploaded_at DESC";

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

                if (dto.getBackToMakerCheques() > 0) {
                    dto.setDisplayStatus("Sent Back");
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
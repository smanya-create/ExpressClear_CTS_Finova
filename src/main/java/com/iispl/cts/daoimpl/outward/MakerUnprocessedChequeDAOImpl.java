package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.MakerUnprocessedChequeDAO;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class MakerUnprocessedChequeDAOImpl implements MakerUnprocessedChequeDAO {

	
	@Override
	public List<UnprocessedChequeDTO> getUnprocessedCheques(String userRole) {
	    List<UnprocessedChequeDTO> list = new ArrayList<>();

	    String sql = "SELECT sc.scanned_cheque_id, sc.scanned_batch_id, " +
	                 "       COALESCE(sb.batch_reference_id, sc.scanned_batch_id) AS batch_ref, " +
	                 "       sc.cheque_number, sc.micr_code, sc.cheque_amount, sc.cheque_status, " +
	                 "       sc.drawee_name, sc.drawee_account_number, sc.created_at " +
	                 "FROM scan_cheque sc " +
	                 "INNER JOIN scan_batch sb ON TRIM(sc.scanned_batch_id) = TRIM(sb.scanned_batch_id) " +
	                 "WHERE ( " +
	                 // Condition A: Cheque itself was marked as UNPROCESSED during EOD
	                 "       UPPER(sc.cheque_status) IN ('UNPROCESSED', 'UNPROCESSED_MICR', 'UNPROCESSED_DATA_ENTRY') " +
	                 "       OR UPPER(sc.cheque_status) LIKE 'UNPROCESSED%' " +
	                 "      ) " +
	                 "   OR ( " +
	                 // Condition B: The parent batch was marked UNPROCESSED / ROLLED_OVER at Forced EOD
	                 "       UPPER(sb.batch_status) IN ('UNPROCESSED', 'ROLLED_OVER', 'FORCED_EOD') " +
	                 "       AND UPPER(sc.cheque_status) IN ('UNPROCESSED', 'UNPROCESSED_MICR', 'UNPROCESSED_DATA_ENTRY', 'PENDING_MICR_REPAIR', 'PENDING_DATA_ENTRY') " +
	                 "      ) " +
	                 "ORDER BY sc.created_at ASC, sc.scanned_cheque_id ASC";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        while (rs.next()) {
	            UnprocessedChequeDTO dto = new UnprocessedChequeDTO();

	            String chqIdStr = rs.getString("scanned_cheque_id");
	            try {
	                dto.setChequeId(Long.parseLong(chqIdStr.replaceAll("\\D+", "")));
	            } catch (Exception e) {
	                dto.setChequeId(0L);
	            }

	            String batchIdStr = rs.getString("scanned_batch_id");
	            try {
	                dto.setBatchId(Long.parseLong(batchIdStr.replaceAll("\\D+", "")));
	            } catch (Exception e) {
	                dto.setBatchId(0L);
	            }

	            dto.setBatchNo(rs.getString("batch_ref"));
	            dto.setOriginalSessionName("Scan Staging");
	            dto.setChequeNo(rs.getString("cheque_number"));

	            String micr = rs.getString("micr_code");
	            dto.setSortCode(micr != null && !micr.trim().isEmpty() ? micr : "------");
	            dto.setAmount(rs.getBigDecimal("cheque_amount"));

	            // Differentiate MICR Repair vs Data Entry
	            String dbStatus = rs.getString("cheque_status");
	            dto.setStatus(dbStatus);
	            String upperStatus = dbStatus != null ? dbStatus.trim().toUpperCase() : "";

	            if ("UNPROCESSED_DATA_ENTRY".equals(upperStatus) || upperStatus.contains("DATA_ENTRY")) {
	                dto.setSendBackReason("Pending Courtesy/Legal Amount Keying (CAR/LAR)");
	            } else if ("UNPROCESSED_MICR".equals(upperStatus) || upperStatus.contains("MICR") || upperStatus.contains("REPAIR")) {
	                dto.setSendBackReason("Defective / Unread MICR Codeline");
	            } else if (micr == null || micr.trim().isEmpty() || micr.contains("?") || "UNREADABLE".equalsIgnoreCase(micr)) {
	                dto.setSendBackReason("Defective / Unread MICR Codeline");
	            } else {
	                dto.setSendBackReason("EOD Rollover Instrument");
	            }

	            dto.setRemarks(chqIdStr + " (" + batchIdStr + ")");
	            dto.setForcedEodRollover(true);
	            list.add(dto);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
	}

	@Override
	public long countPendingRolloverItems() {
	    String sql = "SELECT COUNT(*) FROM scan_cheque sc " +
	                 "INNER JOIN scan_batch sb ON TRIM(sc.scanned_batch_id) = TRIM(sb.scanned_batch_id) " +
	                 "WHERE (UPPER(sc.cheque_status) IN ('UNPROCESSED', 'UNPROCESSED_MICR', 'UNPROCESSED_DATA_ENTRY') OR UPPER(sc.cheque_status) LIKE 'UNPROCESSED%') " +
	                 "   OR (UPPER(sb.batch_status) IN ('UNPROCESSED', 'ROLLED_OVER', 'FORCED_EOD') AND UPPER(sc.cheque_status) IN ('PENDING_MICR_REPAIR', 'PENDING_DATA_ENTRY'))";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        if (rs.next()) {
	            return rs.getLong(1);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}
}
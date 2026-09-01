package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.iispl.cts.common.config.DBConnection;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.stream.Collectors;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.InwardCheque;

public class InwardChequeDAOImpl implements InwardChequeDAO {

	private static InwardChequeDAOImpl instance;
  
    // Static in-memory storage (replace with JDBC PreparedStatement later)
    private final List<InwardCheque> chequeTable = new ArrayList<>();

	private InwardChequeDAOImpl() {
    
     initStaticCheques();
	}

	public static synchronized InwardChequeDAOImpl getInstance() {
		if (instance == null) {
			instance = new InwardChequeDAOImpl();
		}
		return instance;
	}

	// Fetch all inward cheques that require MICR repair.

	@Override
	public List<InwardCheque> getMicrRepairRequiredCheques() {

		List<InwardCheque> list = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, cheque_number, "+ "micr_code, drawee_name, drawee_account_number, "+ "payee_name, payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "+ "WHERE cheque_status = ? " + "ORDER BY created_at ASC";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, "MICR_REPAIR_REQUIRED");

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					list.add(mapResultSet(rs));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load MICR repair records.");

			e.printStackTrace();
		}

		System.out.println("Records loaded: " + list.size());

		return list;
	}

	
	 // Fetch one inward cheque by its ID.
	
	@Override
	public InwardCheque findById(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
			return null;
		}

		String sql = "SELECT inward_cheque_id, inward_batch_id, cheque_number, "+ "micr_code, drawee_name, drawee_account_number, "+ "payee_name, payee_account_number, cheque_amount, "+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				     + "WHERE inward_cheque_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, inwardChequeId.trim());

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {
					return mapResultSet(rs);
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to find cheque: " + inwardChequeId);

			e.printStackTrace();
		}

		return null;
	}

	
	 // Update MICR code and cheque status after repair.
	 
	@Override
	public boolean updateMicrRepair(String inwardChequeId, String correctedMicrCode, String chequeStatus) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty() || correctedMicrCode == null
				    || correctedMicrCode.trim().isEmpty() || chequeStatus == null || chequeStatus.trim().isEmpty())
		
		           {

			          return false;
		           }

		String sql = "UPDATE inward_cheque " + "SET micr_code = ?, cheque_status = ? " + "WHERE inward_cheque_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, correctedMicrCode.trim());
			ps.setString(2, chequeStatus.trim());
			ps.setString(3, inwardChequeId.trim());

			int rowsUpdated = ps.executeUpdate();

			System.out.println("Updated rows: " + rowsUpdated);

			return rowsUpdated > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update MICR repair " + "for cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	
	private InwardCheque mapResultSet(ResultSet rs) throws SQLException {

		InwardCheque cheque = new InwardCheque();

		cheque.setInwardChequeId(rs.getString("inward_cheque_id"));

		cheque.setInwardBatchId(rs.getString("inward_batch_id"));

		cheque.setChequeNumber(rs.getString("cheque_number"));

		cheque.setMicrCode(rs.getString("micr_code"));

		cheque.setDraweeName(rs.getString("drawee_name"));

		cheque.setDraweeAccountNumber(rs.getString("drawee_account_number"));

		cheque.setPayeeName(rs.getString("payee_name"));

		cheque.setPayeeAccountNumber(rs.getString("payee_account_number"));

		cheque.setChequeAmount(rs.getBigDecimal("cheque_amount"));

		cheque.setChequeDate(rs.getDate("cheque_date"));

		cheque.setChequeStatus(rs.getString("cheque_status"));

		cheque.setAccountId(rs.getString("account_id"));

		cheque.setCreatedAt(rs.getTimestamp("created_at"));

		return cheque;
	}
    // Static in-memory storage (replace with JDBC PreparedStatement later)
    private final List<InwardCheque> chequeTable = new ArrayList<>();

    private void initStaticCheques() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // --- BAT1001 Cheques ---
        chequeTable.add(new InwardCheque("CH1005", "BAT1001", "100456", "110532119", 
            "AGARWAL TEXTILES HUF", "07890200001888", "PAWAN KUMAR BANDARU", "5104873962", 
            new BigDecimal("75000.00"), Date.valueOf("2026-04-05"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1006", "BAT1001", "119876", "110532119", 
            "AGARWAL TEXTILES HUF", "07890200001888", "SARITA & ANAND TEXTILES PRIVATE LIMITED", "4256718903", 
            new BigDecimal("3500000.00"), Date.valueOf("2026-03-25"), "PENDING_DATA_ENTRY", null, now));

        // --- BAT1002 Cheques ---
        chequeTable.add(new InwardCheque("CH1007", "BAT1002", "204512", "560220001", 
            "Rajesh Sharma", "000201589632", "Vikram Solar Systems", "995498236410235", 
            new BigDecimal("185000.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1008", "BAT1002", "310890", "400240015", 
            "Meera Nair", "50100234987110", "AGARWAL TEXTILES HUF", "07890200001888", 
            new BigDecimal("450000.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1009", "BAT1002", "552109", "600211008", 
            "K. V. Raman", "918020054321789", "PAWAN KUMAR BANDARU", "5104873962", 
            new BigDecimal("62500.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));
    }

    @Override
    public List<InwardCheque> findByBatchAndStatus(String batchId, String status) {
        return chequeTable.stream()
                .filter(c -> c.getInwardBatchId().equalsIgnoreCase(batchId) 
                          && (status == null || c.getChequeStatus().equalsIgnoreCase(status)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateCheque(InwardCheque cheque) {
        for (int i = 0; i < chequeTable.size(); i++) {
            if (chequeTable.get(i).getInwardChequeId().equals(cheque.getInwardChequeId())) {
                chequeTable.set(i, cheque);
                return true;
            }
        }
        return false;
    }
}
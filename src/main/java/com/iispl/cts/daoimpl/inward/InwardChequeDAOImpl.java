package com.iispl.cts.daoimpl.inward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.InwardCheque;

public class InwardChequeDAOImpl implements InwardChequeDAO {

    // Static in-memory storage (replace with JDBC PreparedStatement later)
    private final List<InwardCheque> chequeTable = new ArrayList<>();

    public InwardChequeDAOImpl() {
        initStaticCheques();
    }

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
    public List<InwardCheque> findByBatchAndStatus(
            String batchId,
            String status) {

        List<InwardCheque> chequeList = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT inward_cheque_id, " +
                "       inward_batch_id, " +
                "       cheque_number, " +
                "       micr_code, " +
                "       drawee_name, " +
                "       drawee_account_number, " +
                "       payee_name, " +
                "       payee_account_number, " +
                "       cheque_amount, " +
                "       cheque_date, " +
                "       cheque_status, " +
                "       account_id, " +
                "       created_at " +
                "FROM inward_cheque " +
                "WHERE inward_batch_id = ? "
        );

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND cheque_status = ? ");
        }

        sql.append("ORDER BY created_at, inward_cheque_id");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, batchId);

            if (status != null && !status.trim().isEmpty()) {
                ps.setString(2, status);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    chequeList.add(mapResultSetToCheque(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chequeList;
    }
    @Override
    public boolean updateCheque(InwardCheque cheque) {

        String sql =
                "UPDATE inward_cheque " +
                "SET cheque_number = ?, " +
                "    micr_code = ?, " +
                "    drawee_name = ?, " +
                "    drawee_account_number = ?, " +
                "    payee_name = ?, " +
                "    payee_account_number = ?, " +
                "    cheque_amount = ?, " +
                "    cheque_date = ?, " +
                "    cheque_status = ?, " +
                "    account_id = ? " +
                "WHERE inward_cheque_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cheque.getChequeNumber());
            ps.setString(2, cheque.getMicrCode());
            ps.setString(3, cheque.getDraweeName());
            ps.setString(4, cheque.getDraweeAccountNumber());
            ps.setString(5, cheque.getPayeeName());
            ps.setString(6, cheque.getPayeeAccountNumber());

            ps.setBigDecimal(7, cheque.getChequeAmount());

            if (cheque.getChequeDate() != null) {
                ps.setDate(
                        8,
                        new Date(cheque.getChequeDate().getTime()));
            } else {
                ps.setDate(8, null);
            }

            ps.setString(9, cheque.getChequeStatus());
            ps.setString(10, cheque.getAccountId());
            ps.setString(11, cheque.getInwardChequeId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    @Override
    public InwardCheque findById(String inwardChequeId) {

        String sql =
                "SELECT inward_cheque_id, " +
                "       inward_batch_id, " +
                "       cheque_number, " +
                "       micr_code, " +
                "       drawee_name, " +
                "       drawee_account_number, " +
                "       payee_name, " +
                "       payee_account_number, " +
                "       cheque_amount, " +
                "       cheque_date, " +
                "       cheque_status, " +
                "       account_id, " +
                "       created_at " +
                "FROM inward_cheque " +
                "WHERE inward_cheque_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inwardChequeId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapResultSetToCheque(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    private InwardCheque mapResultSetToCheque(ResultSet rs)
            throws Exception {

        return new InwardCheque(
                rs.getString("inward_cheque_id"),
                rs.getString("inward_batch_id"),
                rs.getString("cheque_number"),
                rs.getString("micr_code"),
                rs.getString("drawee_name"),
                rs.getString("drawee_account_number"),
                rs.getString("payee_name"),
                rs.getString("payee_account_number"),
                rs.getBigDecimal("cheque_amount"),
                rs.getDate("cheque_date"),
                rs.getString("cheque_status"),
                rs.getString("account_id"),
                rs.getTimestamp("created_at")
        );
    }

}
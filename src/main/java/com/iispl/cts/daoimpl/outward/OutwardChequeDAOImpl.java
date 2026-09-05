package com.iispl.cts.daoimpl.outward;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.entity.outward.OutwardCheque;

public class OutwardChequeDAOImpl implements OutwardChequeDAO {

	

	@Override
	public int getTotalChequeCountByBatchId(String outwardBatchId) {
		String sql = "SELECT COUNT(outward_cheque_id) FROM outward_cheque WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch total cheque count", exception);
		}

		return 0;
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {
		String sql = "SELECT COALESCE(SUM(cheque_amount), 0) FROM outward_cheque WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getBigDecimal(1);
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch total cheque amount", exception);
		}

		return BigDecimal.ZERO;
	}

	private OutwardCheque mapOutwardCheque(ResultSet resultSet) throws Exception {
		OutwardCheque outwardCheque = new OutwardCheque();

		outwardCheque.setOutwardChequeId(resultSet.getString("outward_cheque_id"));
		outwardCheque.setOutwardBatchId(resultSet.getString("outward_batch_id"));
		outwardCheque.setChequeNumber(resultSet.getString("cheque_number"));
		outwardCheque.setMicrCode(resultSet.getString("micr_code"));
		outwardCheque.setDraweeName(resultSet.getString("drawee_name"));
		outwardCheque.setDraweeAccountNumber(resultSet.getString("drawee_account_number"));
		outwardCheque.setPayeeName(resultSet.getString("payee_name"));
		outwardCheque.setPayeeAccountNumber(resultSet.getString("payee_account_number"));
		outwardCheque.setChequeAmount(resultSet.getBigDecimal("cheque_amount"));
		outwardCheque.setChequeDate(resultSet.getDate("cheque_date"));
		outwardCheque.setChequeStatus(resultSet.getString("cheque_status"));
		outwardCheque.setAccountId(resultSet.getString("account_id"));
		outwardCheque.setCreatedAt(resultSet.getTimestamp("created_at"));

		return outwardCheque;
	}

	public void transferChequeFromScanToOutwrd(
	        Connection connection,
	        String scannedBatchId) {

	    if (connection == null) {
	        throw new IllegalArgumentException(
	                "Connection cannot be null");
	    }

	    if (scannedBatchId == null
	            || scannedBatchId.trim().isEmpty()) {

	        throw new IllegalArgumentException(
	                "Scanned batch ID cannot be null or empty");
	    }

	    String sql =
	            "INSERT INTO outward_cheque ("
	            + "outward_batch_id, "
	            + "cheque_number, "
	            + "micr_code, "
	            + "drawee_name, "
	            + "drawee_account_number, "
	            + "payee_name, "
	            + "payee_account_number, "
	            + "cheque_amount, "
	            + "cheque_date, "
	            + "cheque_status, "
	            + "account_id, "
	            + "created_at, "
	            + "city_code, "
	            + "bank_code, "
	            + "branch_code"
	            + ") "
	            + "SELECT "
	            + "ob.outward_batch_id, "
	            + "sc.cheque_number, "
	            + "sc.micr_code, "
	            + "sc.drawee_name, "
	            + "sc.drawee_account_number, "
	            + "sc.payee_name, "
	            + "sc.payee_account_number, "
	            + "sc.cheque_amount, "
	            + "sc.cheque_date, "
	            + "CASE "
	            + "WHEN sc.cheque_status = 'MICR_REPAIR_REQUIRED' "
	            + "THEN 'PENDING_MICR_REPAIR' "
	            + "ELSE 'PENDING_DATA_ENTRY' "
	            + "END, "
	            + "sc.account_id, "
	            + "sc.created_at, "
	            + "sc.city_code, "
	            + "sc.bank_code, "
	            + "sc.branch_code "
	            + "FROM scan_cheque sc "
	            + "INNER JOIN outward_batch ob "
	            + "ON ob.batch_reference_id = ("
	            + "SELECT sb.batch_reference_id "
	            + "FROM scan_batch sb "
	            + "WHERE sb.scanned_batch_id = ?"
	            + ") "
	            + "WHERE sc.scanned_batch_id = ?";

	    try (PreparedStatement ps =
	            connection.prepareStatement(sql)) {

	        ps.setString(1, scannedBatchId);
	        ps.setString(2, scannedBatchId);

	        int rowsInserted =
	                ps.executeUpdate();

	        if (rowsInserted == 0) {

	            throw new IllegalStateException(
	                    "No cheques found for batch ID: "
	                    + scannedBatchId);
	        }

	    } catch (SQLException e) {

	        throw new RuntimeException(
	                "Failed to transfer cheques from scan "
	                + "to outward for batch ID: "
	                + scannedBatchId,
	                e);
	    }
	}

	@Override
	public List<OutwardCheque> getChequesByBatchId(String batchId) {

	    List<OutwardCheque> cheques = new ArrayList<>();

	    if (batchId == null || batchId.trim().isEmpty()) {
	        throw new IllegalArgumentException(
	                "Outward batch ID cannot be null or empty");
	    }

	    String sql =
	            "SELECT " +
	            "    oc.outward_cheque_id, " +
	            "    oc.outward_batch_id, " +
	            "    oc.cheque_number, " +
	            "    oc.micr_code, " +
	            "    oc.drawee_name, " +
	            "    oc.drawee_account_number, " +
	            "    oc.payee_name, " +
	            "    oc.payee_account_number, " +
	            "    oc.cheque_amount, " +
	            "    oc.cheque_date, " +
	            "    oc.cheque_status, " +
	            "    oc.account_id, " +
	            "    oc.created_at, " +
	            "    oc.city_code, " +
	            "    oc.bank_code, " +
	            "    oc.branch_code, " +
	            "    front.image_path AS cheque_image_front, " +
	            "    back.image_path AS cheque_image_back " +

	            "FROM outward_cheque oc " +
	            "LEFT JOIN outward_cheque_image front " +
	            "    ON oc.outward_cheque_id = front.outward_cheque_id " +
	            "    AND front.image_type = 'FRONT' " +

	            "LEFT JOIN outward_cheque_image back " +
	            "    ON oc.outward_cheque_id = back.outward_cheque_id " +
	            "    AND back.image_type = 'BACK' " +

	            "WHERE oc.outward_batch_id = ? " +

	            "ORDER BY oc.outward_cheque_id";

	    try (
	        Connection connection = DBConnection.getConnection();
	        PreparedStatement ps = connection.prepareStatement(sql)
	    ) {

	        ps.setString(1, batchId);

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                OutwardCheque cheque = new OutwardCheque();

	                cheque.setOutwardChequeId(
	                        rs.getString("outward_cheque_id"));

	                cheque.setOutwardBatchId(
	                        rs.getString("outward_batch_id"));

	                cheque.setChequeNumber(
	                        rs.getString("cheque_number"));

	                cheque.setMicrCode(
	                        rs.getString("micr_code"));

	                cheque.setDraweeName(
	                        rs.getString("drawee_name"));

	                cheque.setDraweeAccountNumber(
	                        rs.getString("drawee_account_number"));

	                cheque.setPayeeName(
	                        rs.getString("payee_name"));

	                cheque.setPayeeAccountNumber(
	                        rs.getString("payee_account_number"));

	                cheque.setChequeAmount(
	                        rs.getBigDecimal("cheque_amount"));

	                cheque.setChequeDate(
	                        rs.getDate("cheque_date"));

	                cheque.setChequeStatus(
	                        rs.getString("cheque_status"));

	                cheque.setAccountId(
	                        rs.getString("account_id"));

	                cheque.setCreatedAt(
	                        rs.getTimestamp("created_at"));

	                cheque.setCityCode(
	                        rs.getString("city_code"));

	                cheque.setBankCode(
	                        rs.getString("bank_code"));

	                cheque.setBranchCode(
	                        rs.getString("branch_code"));

	                // IMPORTANT: image paths
	                cheque.setChequeImageFront(
	                        rs.getString("cheque_image_front"));

	                cheque.setChequeImageBack(
	                        rs.getString("cheque_image_back"));

	                // DEBUG
	                System.out.println(
	                        "Cheque Number: " + cheque.getChequeNumber());

	                System.out.println(
	                        "Front Image: " + cheque.getChequeImageFront());

	                System.out.println(
	                        "Back Image: " + cheque.getChequeImageBack());
	               

	                cheques.add(cheque);
	            }
	        }
	        
	    } catch (SQLException e) {

	        throw new RuntimeException(
	                "Error fetching outward cheques for outward batch ID: "
	                        + batchId,
	                e);
	    }

	    return cheques;
	}
}
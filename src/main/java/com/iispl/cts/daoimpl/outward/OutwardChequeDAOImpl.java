package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.entity.outward.OutwardCheque;

public class OutwardChequeDAOImpl implements OutwardChequeDAO{

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {
		 List<OutwardCheque> cheques = new ArrayList<>();

	        String sql =
	                "SELECT outward_cheque_id, "
	              + "outward_batch_id, "
	              + "cheque_number, "
	              + "micr_code, "
	              + "drawee_name, "
	              + "drawee_account_number, "
	              + "payee_name, "
	              + "payee_account_number, "
	              + "cheque_amount, "
	              + "created_at "
	              + "FROM outward_cheque "
	              + "WHERE outward_batch_id = ? "
	              + "ORDER BY outward_cheque_id";

	        try (Connection connection =
	                     DBConnection.getDataSource().getConnection();
	             PreparedStatement ps =
	                     connection.prepareStatement(sql)) {

	            ps.setString(1, outwardBatchId);

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

	                    cheque.setCreatedAt(
	                            rs.getTimestamp("created_at"));

	                    cheques.add(cheque);
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return cheques;
	    }
	
	
	
}

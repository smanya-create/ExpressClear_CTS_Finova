package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.entity.outward.OutwardBatch;

public class OutwardBatchDAOImpl implements OutwardBatchDAO{

	@Override
	public List<OutwardBatch> getVerifiedBatches() {
		List<OutwardBatch> batches = new ArrayList<>();
		 String selectSql =
	                "SELECT outward_batch_id, "
	                + "batch_reference_id, "
	                + "actual_cheque_count, "
	                + "actual_total_amount, "
	                + "batch_status, "
	                + "uploaded_by, "
	                + "uploaded_at "
	                + "FROM outward_batch "
	                + "WHERE batch_status = ?";
		try(Connection connection = DBConnection.getDataSource().getConnection();
				PreparedStatement prepStmt = connection.prepareStatement(selectSql)){
			prepStmt.setString(1,"Pending");
			ResultSet rs = prepStmt.executeQuery();
			while(rs.next()) {
				batches.add(new OutwardBatch(rs.getString("outward_batch_id"),rs.getString("batch_reference_id"),rs.getInt("actual_cheque_count"),rs.getBigDecimal("actual_total_amount"),rs.getString("batch_status"),rs.getString("uploaded_by"),rs.getTimestamp("uploaded_at")));
			}
			
		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
		}
		
		return batches;
	}
	
}
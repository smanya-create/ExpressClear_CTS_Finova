package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardBatchDashboardDAO;
import com.iispl.cts.entity.outward.OutwardBatch;

public class OutwardBatchDashboardDAOImpl implements OutwardBatchDashboardDAO {

	@Override
	public List<OutwardBatch> searchPendingBatches(
	        int pageNumber,
	        int pageSize,
	        String batchId,
	        java.util.Date fromDate,
	        java.util.Date toDate) {

	    List<OutwardBatch> batches = new ArrayList<>();

	    if (pageNumber < 1) {
	        pageNumber = 1;
	    }

	    if (pageSize < 1) {
	        pageSize = 5;
	    }

	    StringBuilder sql = new StringBuilder();

	    sql.append("SELECT outward_batch_id, batch_reference_id, actual_cheque_count, ")
	       .append("actual_total_amount, batch_status, uploaded_by, uploaded_at ")
	       .append("FROM outward_batch ")
	       .append("WHERE UPPER(TRIM(batch_status)) IN (?, ?) ");

	    List<Object> parameters = new ArrayList<>();

	    parameters.add("PENDING_CHECKER_PROCESS");
	    parameters.add("ON_HOLD");

	    if (batchId != null && !batchId.trim().isEmpty()) {
	        sql.append("AND UPPER(outward_batch_id) LIKE UPPER(?) ");
	        parameters.add("%" + batchId.trim() + "%");
	    }

	    if (fromDate != null) {
	        sql.append("AND uploaded_at >= ? ");
	        parameters.add(new java.sql.Timestamp(fromDate.getTime()));
	    }

	    if (toDate != null) {
	        java.util.Calendar calendar = java.util.Calendar.getInstance();
	        calendar.setTime(toDate);
	        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);

	        sql.append("AND uploaded_at < ? ");
	        parameters.add(new java.sql.Timestamp(calendar.getTimeInMillis()));
	    }

	    sql.append("ORDER BY uploaded_at DESC ");
	    sql.append("LIMIT ? OFFSET ?");

	    int offset = (pageNumber - 1) * pageSize;

	    parameters.add(pageSize);
	    parameters.add(offset);

	    try (Connection connection = DBConnection.getConnection();
	            PreparedStatement preparedStatement =
	                    connection.prepareStatement(sql.toString())) {

	        for (int index = 0; index < parameters.size(); index++) {

	            Object parameter = parameters.get(index);

	            if (parameter instanceof Integer) {
	                preparedStatement.setInt(index + 1, (Integer) parameter);
	            } else if (parameter instanceof java.sql.Timestamp) {
	                preparedStatement.setTimestamp(
	                        index + 1, (java.sql.Timestamp) parameter);
	            } else {
	                preparedStatement.setString(
	                        index + 1, parameter.toString());
	            }
	        }

	        try (ResultSet resultSet = preparedStatement.executeQuery()) {

	            while (resultSet.next()) {
	                batches.add(mapOutwardBatch(resultSet));
	            }
	        }

	    } catch (SQLException exception) {
	        throw new RuntimeException(
	                "Unable to search outward batches", exception);
	    }

	    return batches;
	}
	
	 private OutwardBatch mapOutwardBatch(ResultSet resultSet)
	            throws SQLException {

	        OutwardBatch outwardBatch = new OutwardBatch();

	        outwardBatch.setOutwardBatchId(
	                resultSet.getString("outward_batch_id"));

	        outwardBatch.setBatchReferenceId(
	                resultSet.getString("batch_reference_id"));

	        outwardBatch.setActualChequeCount(
	                resultSet.getInt("actual_cheque_count"));

	        outwardBatch.setActualTotalAmount(
	                resultSet.getBigDecimal("actual_total_amount"));

	        outwardBatch.setBatchStatus(
	                resultSet.getString("batch_status"));

	        outwardBatch.setUploadedBy(
	                resultSet.getString("uploaded_by"));

	        outwardBatch.setUploadedAt(
	                resultSet.getTimestamp("uploaded_at"));

	        return outwardBatch;
	    }

	 @Override
	 public int getSearchPendingBatchCount(
	         String batchId,
	         java.util.Date fromDate,
	         java.util.Date toDate) {

	     StringBuilder sql = new StringBuilder();

	     sql.append("SELECT COUNT(*) ")
	        .append("FROM outward_batch ")
	        .append("WHERE UPPER(TRIM(batch_status)) IN (?, ?) ");

	     List<Object> parameters = new ArrayList<>();

	     parameters.add("PENDING_CHECKER_PROCESS");
	     parameters.add("ON_HOLD");

	     if (batchId != null && !batchId.trim().isEmpty()) {
	         sql.append("AND UPPER(outward_batch_id) LIKE UPPER(?) ");
	         parameters.add("%" + batchId.trim() + "%");
	     }

	     if (fromDate != null) {
	         sql.append("AND uploaded_at >= ? ");
	         parameters.add(new java.sql.Timestamp(fromDate.getTime()));
	     }

	     if (toDate != null) {
	         java.util.Calendar calendar = java.util.Calendar.getInstance();
	         calendar.setTime(toDate);
	         calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);

	         sql.append("AND uploaded_at < ? ");
	         parameters.add(new java.sql.Timestamp(calendar.getTimeInMillis()));
	     }

	     try (Connection connection = DBConnection.getConnection();
	             PreparedStatement preparedStatement =
	                     connection.prepareStatement(sql.toString())) {

	         for (int index = 0; index < parameters.size(); index++) {

	             Object parameter = parameters.get(index);

	             if (parameter instanceof java.sql.Timestamp) {
	                 preparedStatement.setTimestamp(
	                         index + 1, (java.sql.Timestamp) parameter);
	             } else {
	                 preparedStatement.setString(
	                         index + 1, parameter.toString());
	             }
	         }

	         try (ResultSet resultSet = preparedStatement.executeQuery()) {

	             if (resultSet.next()) {
	                 return resultSet.getInt(1);
	             }
	         }

	     } catch (SQLException exception) {
	         throw new RuntimeException(
	                 "Unable to count searched outward batches", exception);
	     }

	     return 0;
	 }

	
}

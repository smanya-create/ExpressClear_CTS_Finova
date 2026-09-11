package com.iispl.cts.daoimpl.inward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;

public class InwardChequeDAOImpl implements InwardChequeDAO {

	private static InwardChequeDAOImpl instance;

	private InwardChequeDAOImpl() {
	}

	public static synchronized InwardChequeDAOImpl getInstance() {

		if (instance == null) {
			instance = new InwardChequeDAOImpl();
		}

		return instance;
	}

	private static final String SELECT_COLUMNS = "inward_cheque_id, " + "inward_batch_id, " + "cheque_number, "
			+ "micr_code, " + "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
			+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, " + "city_code, "
			+ "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back, " + "transaction_code, "
			+ "item_sequence_number ";

	@Override
	public List<InwardCheque> getMicrRepairRequiredCheques() {

		List<InwardCheque> list = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "WHERE cheque_status = ? "
				+ "ORDER BY item_sequence_number ASC, created_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, "MICR_REPAIR_REQUIRED");

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

					list.add(mapResultSet(resultSet));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load MICR repair records.");

			e.printStackTrace();
		}

		System.out.println("MICR repair records loaded: " + list.size());

		return list;
	}

	@Override
	public InwardCheque findById(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {

			return null;
		}

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId.trim());

			try (ResultSet resultSet = statement.executeQuery()) {

				if (resultSet.next()) {

					return mapResultSet(resultSet);
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to find cheque: " + inwardChequeId);

			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<InwardCheque> findByBatchAndStatus(String batchId, String status) {

		List<InwardCheque> cheques = new ArrayList<>();

		if (batchId == null || batchId.trim().isEmpty()) {

			return cheques;
		}

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ").append(SELECT_COLUMNS).append("FROM inward_cheque ").append("WHERE inward_batch_id = ? ");

		if (status != null && !status.trim().isEmpty()) {

			sql.append("AND cheque_status = ? ");
		}

		sql.append("ORDER BY item_sequence_number ASC, created_at ASC");

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql.toString())) {

			statement.setString(1, batchId.trim());

			if (status != null && !status.trim().isEmpty()) {

				statement.setString(2, status.trim());
			}

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

					cheques.add(mapResultSet(resultSet));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load cheques for batch: " + batchId);

			e.printStackTrace();
		}

		return cheques;
	}
	
	@Override
	public String getBankNameByCode(String bankCode) {

	    String sql = "SELECT bank_name "
	               + "FROM master_bank "
	               + "WHERE bank_code = ?";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, bankCode);

	        try (ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {
	                return rs.getString("bank_name");
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	@Override
	public List<InwardCheque> getAllCheques() {

		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "ORDER BY created_at DESC";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql);

				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

				cheques.add(mapResultSet(resultSet));
			}

		} catch (SQLException e) {

			System.err.println("Failed to load all inward cheques.");

			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public InwardCheque getChequeById(String inwardChequeId) {

		return findById(inwardChequeId);
	}
	
	@Override
	public String getRejectedReasonDetails(String inwardChequeId) {

	    String sql =
	            "SELECT rr.rejected_reason_code, " +
	            "       rr.rejected_reason_name, " +
	            "       r.remarks " +
	            "FROM inward_cheque_rejection r " +
	            "LEFT JOIN rejected_reasons rr " +
	            "       ON rr.rejected_reason_id = r.rejected_reason_id " +
	            "WHERE r.inward_cheque_id = ? " +
	            "ORDER BY r.rejected_at DESC NULLS LAST " +
	            "LIMIT 1";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, inwardChequeId);

	        try (ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {

	                String reasonCode =
	                        rs.getString("rejected_reason_code");

	                String reasonName =
	                        rs.getString("rejected_reason_name");

	                String remarks =
	                        rs.getString("remarks");

	                StringBuilder result =
	                        new StringBuilder();

	                result.append("Reason: ")
	                      .append(reasonCode)
	                      .append(" - ")
	                      .append(reasonName);

	                if (remarks != null &&
	                    !remarks.trim().isEmpty()) {

	                    result.append("\nRemarks: ")
	                          .append(remarks.trim());
	                }

	                return result.toString();
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}
	
	
	
	
	@Override
	public BigDecimal getAccountBalance(String accountNumber) {

	    if (accountNumber == null || accountNumber.trim().isEmpty()) {
	        return null;
	    }

	    String sql =
	            "SELECT account_balance " +
	            "FROM master_account_new " +
	            "WHERE account_number = ?";

	    try (Connection connection = DBConnection.getConnection();
	         PreparedStatement statement =
	                 connection.prepareStatement(sql)) {

	        statement.setString(1, accountNumber.trim());

	        try (ResultSet resultSet = statement.executeQuery()) {

	            if (resultSet.next()) {
	                return resultSet.getBigDecimal("account_balance");
	            }
	        }

	    } catch (SQLException e) {

	        System.err.println(
	                "Failed to fetch account balance for account: "
	                + accountNumber);

	        e.printStackTrace();
	    }

	    return null;
	}
	
	@Override
	public String getMakerRejectionRequestDetails(String inwardChequeId) {

	    String sql =
	            "SELECT rejected_reason_id, remarks " +
	            "FROM inward_cheque_rejection_request " +
	            "WHERE inward_cheque_id = ? " +
	            "AND request_status = 'PENDING' " +
	            "ORDER BY requested_at DESC " +
	            "LIMIT 1";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, inwardChequeId);

	        try (ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {

	                String rejectedReasonId =
	                        rs.getString("rejected_reason_id");

	                String remarks =
	                        rs.getString("remarks");

	                return rejectedReasonId + "||" +
	                       (remarks != null ? remarks : "");
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	@Override
	public List<InwardCheque> getChequesByBatchId(String inwardBatchId) {

		return findByBatchAndStatus(inwardBatchId, null);
	}

	@Override
	public boolean saveCheque(InwardCheque inwardCheque) {

		if (inwardCheque == null) {

			return false;
		}

		String sql = "INSERT INTO inward_cheque (" + "inward_cheque_id, " + "inward_batch_id, " + "cheque_number, "
				+ "micr_code, " + "drawee_name, " + "drawee_account_number, " + "payee_name, "
				+ "payee_account_number, " + "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, "
				+ "created_at, " + "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, "
				+ "cheque_image_back, " + "transaction_code, " + "item_sequence_number" + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?" + ")";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardCheque.getInwardChequeId());

			statement.setString(2, inwardCheque.getInwardBatchId());

			statement.setString(3, inwardCheque.getChequeNumber());

			statement.setString(4, inwardCheque.getMicrCode());

			statement.setString(5, inwardCheque.getDraweeName());

			statement.setString(6, inwardCheque.getDraweeAccountNumber());

			statement.setString(7, inwardCheque.getPayeeName());

			statement.setString(8, inwardCheque.getPayeeAccountNumber());

			statement.setBigDecimal(9, inwardCheque.getChequeAmount());

			statement.setDate(10, inwardCheque.getChequeDate());

			statement.setString(11, inwardCheque.getChequeStatus());

			statement.setString(12, inwardCheque.getAccountId());

			statement.setTimestamp(13, inwardCheque.getCreatedAt());

			statement.setString(14, inwardCheque.getCityCode());

			statement.setString(15, inwardCheque.getBankCode());

			statement.setString(16, inwardCheque.getBranchCode());

			statement.setString(17, inwardCheque.getChequeImageFront());

			statement.setString(18, inwardCheque.getChequeImageBack());

			statement.setString(19, inwardCheque.getTransactionCode());

			if (inwardCheque.getItemSequenceNumber() != null) {

				statement.setInt(20, inwardCheque.getItemSequenceNumber());

			} else {

				statement.setNull(20, java.sql.Types.INTEGER);
			}

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to save inward cheque: " + inwardCheque.getInwardChequeId());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean updateCheque(InwardCheque inwardCheque) {

		if (inwardCheque == null || inwardCheque.getInwardChequeId() == null
				|| inwardCheque.getInwardChequeId().trim().isEmpty()) {

			return false;
		}

		String sql = "UPDATE inward_cheque SET " + "inward_batch_id = ?, " + "cheque_number = ?, " + "micr_code = ?, "
				+ "drawee_name = ?, " + "drawee_account_number = ?, " + "payee_name = ?, "
				+ "payee_account_number = ?, " + "cheque_amount = ?, " + "cheque_date = ?, " + "cheque_status = ?, "
				+ "account_id = ?, " + "created_at = ?, " + "city_code = ?, " + "bank_code = ?, " + "branch_code = ?, "
				+ "cheque_image_front = ?, " + "cheque_image_back = ?, " + "transaction_code = ?, "
				+ "item_sequence_number = ? " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardCheque.getInwardBatchId());

			statement.setString(2, inwardCheque.getChequeNumber());

			statement.setString(3, inwardCheque.getMicrCode());

			statement.setString(4, inwardCheque.getDraweeName());

			statement.setString(5, inwardCheque.getDraweeAccountNumber());

			statement.setString(6, inwardCheque.getPayeeName());

			statement.setString(7, inwardCheque.getPayeeAccountNumber());

			statement.setBigDecimal(8, inwardCheque.getChequeAmount());

			statement.setDate(9, inwardCheque.getChequeDate());

			statement.setString(10, inwardCheque.getChequeStatus());

			statement.setString(11, inwardCheque.getAccountId());

			statement.setTimestamp(12, inwardCheque.getCreatedAt());

			statement.setString(13, inwardCheque.getCityCode());

			statement.setString(14, inwardCheque.getBankCode());

			statement.setString(15, inwardCheque.getBranchCode());

			statement.setString(16, inwardCheque.getChequeImageFront());

			statement.setString(17, inwardCheque.getChequeImageBack());

			statement.setString(18, inwardCheque.getTransactionCode());

			if (inwardCheque.getItemSequenceNumber() != null) {

				statement.setInt(19, inwardCheque.getItemSequenceNumber());

			} else {

				statement.setNull(19, java.sql.Types.INTEGER);
			}

			statement.setString(20, inwardCheque.getInwardChequeId());

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update inward cheque: " + inwardCheque.getInwardChequeId());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean updateMicrRepair(String inwardChequeId, String inwardBatchId, String originalMicr,
			String correctedMicrCode, String chequeStatus, String repairedBy, String remarks) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty() || inwardBatchId == null
				|| inwardBatchId.trim().isEmpty() || originalMicr == null || originalMicr.trim().isEmpty()
				|| correctedMicrCode == null || correctedMicrCode.trim().isEmpty() || chequeStatus == null
				|| chequeStatus.trim().isEmpty()) {

			return false;
		}

		String historySql = "INSERT INTO inward_micr_repair_history "
				+ "(inward_cheque_id, inward_batch_id, original_micr, "
				+ "corrected_micr, repaired_by, repaired_at, repair_status, remarks) "
				+ "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";

		String updateSql = "UPDATE inward_cheque " + "SET micr_code = ?, cheque_status = ? "
				+ "WHERE inward_cheque_id = ?";

		Connection connection = null;

		try {

			connection = DBConnection.getConnection();

			connection.setAutoCommit(false);

			try (PreparedStatement historyStatement = connection.prepareStatement(historySql)) {

				historyStatement.setString(1, inwardChequeId.trim());

				historyStatement.setString(2, inwardBatchId.trim());

				historyStatement.setString(3, originalMicr.trim());

				historyStatement.setString(4, correctedMicrCode.trim());

				if (repairedBy != null && !repairedBy.trim().isEmpty()) {

					historyStatement.setString(5, repairedBy.trim());

				} else {

					historyStatement.setNull(5, java.sql.Types.VARCHAR);
				}

				historyStatement.setString(6, "COMPLETED");

				if (remarks != null && !remarks.trim().isEmpty()) {

					historyStatement.setString(7, remarks.trim());

				} else {

					historyStatement.setNull(7, java.sql.Types.VARCHAR);
				}

				historyStatement.executeUpdate();
			}

			try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

				updateStatement.setString(1, correctedMicrCode.trim());

				updateStatement.setString(2, chequeStatus.trim());

				updateStatement.setString(3, inwardChequeId.trim());

				int rowsUpdated = updateStatement.executeUpdate();

				if (rowsUpdated <= 0) {

					connection.rollback();

					return false;
				}
			}

			connection.commit();

			return true;

		} catch (SQLException e) {

			if (connection != null) {

				try {
					connection.rollback();
				} catch (SQLException rollbackException) {
					rollbackException.printStackTrace();
				}
			}

			System.err.println("Failed to save MICR repair history/update for cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;

		} finally {

			if (connection != null) {

				try {
					connection.setAutoCommit(true);
					connection.close();
				} catch (SQLException closeException) {
					closeException.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean deleteCheque(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {

			return false;
		}

		String sql = "DELETE FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId.trim());

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to delete inward cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	private InwardCheque mapResultSet(ResultSet resultSet) throws SQLException {

		InwardCheque cheque = new InwardCheque();

		cheque.setInwardChequeId(resultSet.getString("inward_cheque_id"));

		cheque.setInwardBatchId(resultSet.getString("inward_batch_id"));

		cheque.setChequeNumber(resultSet.getString("cheque_number"));

		cheque.setMicrCode(resultSet.getString("micr_code"));

		cheque.setDraweeName(resultSet.getString("drawee_name"));

		cheque.setDraweeAccountNumber(resultSet.getString("drawee_account_number"));

		cheque.setPayeeName(resultSet.getString("payee_name"));

		cheque.setPayeeAccountNumber(resultSet.getString("payee_account_number"));

		cheque.setChequeAmount(resultSet.getBigDecimal("cheque_amount"));

		cheque.setChequeDate(resultSet.getDate("cheque_date"));

		cheque.setChequeStatus(resultSet.getString("cheque_status"));

		cheque.setAccountId(resultSet.getString("account_id"));

		cheque.setCreatedAt(resultSet.getTimestamp("created_at"));

		cheque.setCityCode(resultSet.getString("city_code"));

		cheque.setBankCode(resultSet.getString("bank_code"));

		cheque.setBranchCode(resultSet.getString("branch_code"));

		cheque.setChequeImageFront(resultSet.getString("cheque_image_front"));

		cheque.setChequeImageBack(resultSet.getString("cheque_image_back"));

		cheque.setTransactionCode(resultSet.getString("transaction_code"));

		int sequence = resultSet.getInt("item_sequence_number");

		if (!resultSet.wasNull()) {

			cheque.setItemSequenceNumber(sequence);
		}

		return cheque;
	}

	@Override
	public boolean saveRejection(String inwardChequeId, String rejectedReasonId, String remarks, String rejectedBy) {

		String sql = "INSERT INTO inward_cheque_rejection "
				+ "(inward_cheque_id, rejected_reason_id, remarks, rejected_by) " + "VALUES (?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, inwardChequeId);
			ps.setString(2, rejectedReasonId);
			ps.setString(3, remarks);
			ps.setString(4, rejectedBy);

			int rowsInserted = ps.executeUpdate();

			System.out.println("Rejection record inserted. Rows: " + rowsInserted);

			return rowsInserted > 0;

		} catch (SQLException e) {

			System.err.println("Failed to save rejection for cheque: " + inwardChequeId);

			System.err.println("Reason ID: " + rejectedReasonId);

			System.err.println("Remarks: " + remarks);

			System.err.println("Rejected By: " + rejectedBy);

			System.err.println("SQL Error: " + e.getMessage());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean saveRejectionRequest(InwardChequeRejectionRequest request) {

		String sql = "INSERT INTO inward_cheque_rejection_request "
				+ "(inward_cheque_id, inward_batch_id, rejected_reason_id, "
				+ "remarks, requested_by, request_stage, request_status) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, request.getInwardChequeId());
			ps.setString(2, request.getInwardBatchId());
			ps.setString(3, request.getRejectedReasonId());
			ps.setString(4, request.getRemarks());
			ps.setString(5, request.getRequestedBy());
			ps.setString(6, request.getRequestStage());

			ps.setString(7, request.getRequestStatus() != null ? request.getRequestStatus() : "PENDING");

			int rowsInserted = ps.executeUpdate();

			System.out.println("Rejection request inserted. Rows: " + rowsInserted);

			return rowsInserted > 0;

		} catch (SQLException e) {

			System.err.println("Failed to save rejection request for cheque: " + request.getInwardChequeId());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean updateChequeStatus(String inwardChequeId, String chequeStatus) {

		String sql = "UPDATE inward_cheque " + "SET cheque_status = ? " + "WHERE inward_cheque_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, chequeStatus);
			ps.setString(2, inwardChequeId);

			int rowsUpdated = ps.executeUpdate();

			System.out.println("Cheque status updated. Cheque: " + inwardChequeId + ", Status: " + chequeStatus
					+ ", Rows: " + rowsUpdated);

			return rowsUpdated > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update cheque status for: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public CbsValidationResult validateCbs(InwardCheque cheque) {
		String sql = "SELECT " + "    a.account_number, " + "    a.account_holder_name, " + "    a.account_balance, "
				+ "    a.account_status, " + "    mc.cheque_number AS master_cheque_number, " + "    mc.sort_code, "
				+ "    b.branch_code AS master_branch_code, " + "    b.micr_code AS master_micr_code, "
				+ "    b.status AS branch_status, " + "    bk.bank_code AS master_bank_code, "
				+ "    bk.status AS bank_status " + "FROM master_account_new a " + "LEFT JOIN master_cheque_new mc "
				+ "    ON mc.account_number = a.account_number " + "   AND mc.cheque_number = ? "
				+ "LEFT JOIN branch b " + "    ON b.branch_code = ? " + "LEFT JOIN bank bk "
				+ "    ON bk.bank_id = b.bank_id " + "WHERE a.account_number = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cheque.getChequeNumber());
			ps.setString(2, cheque.getBranchCode());
			ps.setString(3, cheque.getDraweeAccountNumber());

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {

					System.out.println("CBS Validation Failed: Account not found - " + cheque.getDraweeAccountNumber());

					return new CbsValidationResult(false, "Account not found: " + cheque.getDraweeAccountNumber());
				}

				String accountStatus = rs.getString("account_status");

				if (!"Active".equalsIgnoreCase(accountStatus)) {

					System.out.println("CBS Validation Failed: Account is " + accountStatus);

					return new CbsValidationResult(false, "Account is " + accountStatus + ".");
				}

				String accountHolderName = rs.getString("account_holder_name");

				if (cheque.getDraweeName() == null || accountHolderName == null
						|| !cheque.getDraweeName().trim().equalsIgnoreCase(accountHolderName.trim())) {

					System.out.println("CBS Validation Failed: " + "Account holder name mismatch.");

					return new CbsValidationResult(false, "Account holder name mismatch.");
				}

				String masterChequeNumber = rs.getString("master_cheque_number");

				if (masterChequeNumber == null) {

					System.out.println("CBS Validation Failed: Cheque number " + cheque.getChequeNumber()
							+ " does not exist for account " + cheque.getDraweeAccountNumber());

					return new CbsValidationResult(false,
							"Cheque number " + cheque.getChequeNumber() + " does not exist for this account.");
				}

				String masterBankCode = rs.getString("master_bank_code");

				if (masterBankCode == null || cheque.getBankCode() == null
						|| !cheque.getBankCode().trim().equalsIgnoreCase(masterBankCode.trim())) {

					System.out.println("CBS Validation Failed: Bank code mismatch.");

					return new CbsValidationResult(false,
							"Bank code mismatch. " + "Cheque: " + cheque.getBankCode() + ", Master: " + masterBankCode);
				}

				String masterBranchCode = rs.getString("master_branch_code");

				if (masterBranchCode == null || cheque.getBranchCode() == null
						|| !cheque.getBranchCode().trim().equalsIgnoreCase(masterBranchCode.trim())) {

					System.out.println("CBS Validation Failed: Branch code mismatch.");

					return new CbsValidationResult(false, "Branch code mismatch. " + "Cheque: " + cheque.getBranchCode()
							+ ", Master: " + masterBranchCode);
				}

				String masterMicrCode = rs.getString("master_micr_code");

				if (masterMicrCode == null || cheque.getMicrCode() == null
						|| !cheque.getMicrCode().trim().equalsIgnoreCase(masterMicrCode.trim())) {

					System.out.println("CBS Validation Failed: MICR code mismatch.");

					return new CbsValidationResult(false,
							"MICR code mismatch. " + "Cheque: " + cheque.getMicrCode() + ", Master: " + masterMicrCode);
				}

				String branchStatus = rs.getString("branch_status");

				if (!"Active".equalsIgnoreCase(branchStatus)) {

					System.out.println("CBS Validation Failed: Branch is " + branchStatus);

					return new CbsValidationResult(false, "Branch is " + branchStatus + ".");
				}

				String bankStatus = rs.getString("bank_status");

				if (!"Active".equalsIgnoreCase(bankStatus)) {

					System.out.println("CBS Validation Failed: Bank is " + bankStatus);

					return new CbsValidationResult(false, "Bank is " + bankStatus + ".");
				}

				if (cheque.getTransactionCode() == null || cheque.getTransactionCode().trim().isEmpty()) {

					System.out.println("CBS Validation Failed: " + "Transaction code is missing.");

					return new CbsValidationResult(false, "Transaction code is missing.");
				}

				if (cheque.getChequeDate() == null) {

					System.out.println("CBS Validation Failed: " + "Cheque date is missing.");

					return new CbsValidationResult(false, "Cheque date is missing.");
				}

				java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

				if (cheque.getChequeDate().after(today)) {

					System.out.println("CBS Validation Failed: " + "Cheque is postdated.");

					return new CbsValidationResult(false,
							"Cheque is postdated. " + "Cheque date: " + cheque.getChequeDate());
				}

				BigDecimal accountBalance = rs.getBigDecimal("account_balance");

				if (accountBalance == null || cheque.getChequeAmount() == null
						|| accountBalance.compareTo(cheque.getChequeAmount()) < 0) {

					System.out.println("CBS Validation Failed: " + "Insufficient balance.");

					return new CbsValidationResult(false, "Insufficient balance. " + "Available: ₹" + accountBalance
							+ ", Cheque amount: ₹" + cheque.getChequeAmount());
				}

				System.out.println("CBS Validation Passed for cheque: " + cheque.getChequeNumber());

				return new CbsValidationResult(true, "CBS validation passed.");

			}

		} catch (SQLException e) {

			System.err.println("CBS Validation Error for cheque: " + cheque.getChequeNumber());

			e.printStackTrace();

			return new CbsValidationResult(false, "Unable to perform CBS validation because of a database error.");
		}
	}
}
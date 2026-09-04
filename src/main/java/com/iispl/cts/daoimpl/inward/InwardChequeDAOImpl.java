package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
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
	    

	 


	 



	@Override
	public List<InwardCheque> getAllCheques() {
		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "ORDER BY created_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

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

				cheques.add(cheque);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public InwardCheque getChequeById(String inwardChequeId) {

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId);

			try (ResultSet resultSet = statement.executeQuery()) {

				if (resultSet.next()) {

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

					return cheque;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	

	@Override
	public List<InwardCheque> getChequesByBatchId(String inwardBatchId) {

		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "WHERE inward_batch_id = ? " + "ORDER BY created_at";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardBatchId);

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

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

					cheques.add(cheque);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public boolean saveCheque(InwardCheque inwardCheque) {

		String sql = "INSERT INTO inward_cheque " + "(inward_cheque_id, inward_batch_id, "
				+ "cheque_number, micr_code, drawee_name, " + "drawee_account_number, payee_name, "
				+ "payee_account_number, cheque_amount, " + "cheque_date, cheque_status, account_id, created_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updatecheque(InwardCheque inwardCheque) {

		String sql = "UPDATE inward_cheque SET " + "inward_batch_id = ?, " + "cheque_number = ?, " + "micr_code = ?, "
				+ "drawee_name = ?, " + "drawee_account_number = ?, " + "payee_name = ?, "
				+ "payee_account_number = ?, " + "cheque_amount = ?, " + "cheque_date = ?, " + "cheque_status = ?, "
				+ "account_id = ?, " + "created_at = ? " + "WHERE inward_cheque_id = ?";

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
			statement.setString(13, inwardCheque.getInwardChequeId());

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteCheque(String inwardChequeId) {

		String sql = "DELETE FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId);

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
/*=======================================================================================*/

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
       private InwardCheque mapResultSetToCheque(ResultSet rs)
            throws Exception {
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
       
       

}
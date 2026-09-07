package com.iispl.cts.daoimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardCheckerRejectionDAO;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;

public class OutwardCheckerRejectionDAOImpl implements OutwardCheckerRejectionDAO {

	// =========================================================
	// GET PAGINATED REJECTED CHEQUES
	// =========================================================

	@Override
	public List<OutwardRejectedCheques> getRejectedCheques(int limit, int offset) throws Exception {

		List<OutwardRejectedCheques> rejectedCheques = new ArrayList<>();

		String sql = "SELECT " + "outward_rejected_cheque_id, " + "outward_cheque_id, " + "rejected_by, "
				+ "rejected_date, " + "remarks, " + "outward_batch_id, " + "cheque_amount "
				+ "FROM public.outward_rejected_cheques " + "ORDER BY rejected_date DESC " + "LIMIT ? OFFSET ?";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setInt(1, limit);
			ps.setInt(2, offset);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					OutwardRejectedCheques rejectedCheque = mapRejectedCheque(rs);

					rejectedCheques.add(rejectedCheque);
				}
			}
		}

		return rejectedCheques;
	}

	// =========================================================
	// SEARCH + PAGINATION
	// =========================================================

	@Override
	public List<OutwardRejectedCheques> searchRejectedCheques(String searchValue, Date rejectedDate, int limit,
			int offset) throws Exception {

		List<OutwardRejectedCheques> rejectedCheques = new ArrayList<>();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT " + "outward_rejected_cheque_id, " + "outward_cheque_id, " + "rejected_by, "
				+ "rejected_date, " + "remarks, " + "outward_batch_id, " + "cheque_amount "
				+ "FROM public.outward_rejected_cheques " + "WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();

		// -----------------------------------------------------
		// SEARCH TEXT
		// -----------------------------------------------------

		if (searchValue != null && !searchValue.trim().isEmpty()) {

			sql.append("AND (" + "LOWER(outward_rejected_cheque_id) LIKE ? " + "OR LOWER(outward_cheque_id) LIKE ? "
					+ "OR LOWER(outward_batch_id) LIKE ? " + "OR LOWER(rejected_by) LIKE ? "
					+ "OR LOWER(remarks) LIKE ?" + ") ");

			String searchPattern = "%" + searchValue.trim().toLowerCase() + "%";

			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
		}

		// -----------------------------------------------------
		// SEARCH DATE
		// -----------------------------------------------------

		if (rejectedDate != null) {

			sql.append("AND DATE(rejected_date) = ? ");

			parameters.add(rejectedDate);
		}

		// -----------------------------------------------------
		// ORDER + PAGINATION
		// -----------------------------------------------------

		sql.append("ORDER BY rejected_date DESC " + "LIMIT ? OFFSET ?");

		parameters.add(limit);
		parameters.add(offset);

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {

				ps.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					OutwardRejectedCheques rejectedCheque = mapRejectedCheque(rs);

					rejectedCheques.add(rejectedCheque);
				}
			}
		}

		return rejectedCheques;
	}

	// =========================================================
	// TOTAL ALL RECORDS
	// =========================================================

	@Override
	public int getTotalRejectedCheques() throws Exception {

		String sql = "SELECT COUNT(*) " + "FROM public.outward_rejected_cheques";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				return rs.getInt(1);
			}
		}

		return 0;
	}

	// =========================================================
	// TOTAL SEARCH RESULTS
	// =========================================================

	@Override
	public int getTotalRejectedCheques(String searchValue, Date rejectedDate) throws Exception {

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(*) " + "FROM public.outward_rejected_cheques " + "WHERE 1=1 ");

		List<Object> parameters = new ArrayList<>();

		// -----------------------------------------------------
		// SEARCH TEXT
		// -----------------------------------------------------

		if (searchValue != null && !searchValue.trim().isEmpty()) {

			sql.append("AND (" + "LOWER(outward_rejected_cheque_id) LIKE ? " + "OR LOWER(outward_cheque_id) LIKE ? "
					+ "OR LOWER(outward_batch_id) LIKE ? " + "OR LOWER(rejected_by) LIKE ? "
					+ "OR LOWER(remarks) LIKE ?" + ") ");

			String searchPattern = "%" + searchValue.trim().toLowerCase() + "%";

			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
			parameters.add(searchPattern);
		}

		// -----------------------------------------------------
		// SEARCH DATE
		// -----------------------------------------------------

		if (rejectedDate != null) {

			sql.append("AND DATE(rejected_date) = ? ");

			parameters.add(rejectedDate);
		}

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql.toString())) {

			for (int i = 0; i < parameters.size(); i++) {

				ps.setObject(i + 1, parameters.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}

		return 0;
	}

	// =========================================================
	// SAVE
	// =========================================================

	@Override
	public boolean saveRejectedCheque(OutwardRejectedCheques rejectedCheque) throws Exception {

		String sql = "INSERT INTO public.outward_rejected_cheques " + "(" + "outward_cheque_id, " + "rejected_by, "
				+ "remarks, " + "outward_batch_id, " + "cheque_amount" + ") " + "VALUES (?, ?, ?, ?, ?)";

		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, rejectedCheque.getOutwardChequeId());

			ps.setString(2, rejectedCheque.getRejectedBy());

			ps.setString(3, rejectedCheque.getRemarks());

			ps.setString(4, rejectedCheque.getOutwardBatchId());

			ps.setBigDecimal(5, rejectedCheque.getChequeAmount());

			return ps.executeUpdate() > 0;
		}
	}

	// =========================================================
	// COMMON MAPPING METHOD
	// =========================================================

	private OutwardRejectedCheques mapRejectedCheque(ResultSet rs) throws Exception {

		OutwardRejectedCheques rejectedCheque = new OutwardRejectedCheques();

		rejectedCheque.setOutwardRejectedChequeId(rs.getString("outward_rejected_cheque_id"));

		rejectedCheque.setOutwardChequeId(rs.getString("outward_cheque_id"));

		rejectedCheque.setRejectedBy(rs.getString("rejected_by"));

		rejectedCheque.setRejectedDate(rs.getTimestamp("rejected_date"));

		rejectedCheque.setRemarks(rs.getString("remarks"));

		rejectedCheque.setOutwardBatchId(rs.getString("outward_batch_id"));

		BigDecimal amount = rs.getBigDecimal("cheque_amount");

		rejectedCheque.setChequeAmount(amount);

		return rejectedCheque;
	}
}
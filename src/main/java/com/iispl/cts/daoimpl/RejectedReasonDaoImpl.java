package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.RejectedReasonDao;
import com.iispl.cts.entity.RejectedReason;

public class RejectedReasonDaoImpl implements RejectedReasonDao {

	private static RejectedReasonDaoImpl instance;

	private RejectedReasonDaoImpl() {
	}

	public static synchronized RejectedReasonDaoImpl getInstance() {

		if (instance == null) {
			instance = new RejectedReasonDaoImpl();
		}

		return instance;
	}

	// Fetch all rejection reasons.
	@Override
	public List<RejectedReason> getAllRejectedReasons() {

		List<RejectedReason> list = new ArrayList<>();

		String sql = "SELECT rejected_reason_id, rejected_reason_code, "+ "rejected_reason_name, rejected_reason_description " + "FROM rejected_reasons "
				      + "ORDER BY rejected_reason_code ASC";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSet(rs));
			}

		} catch (SQLException e) {

			System.err.println("Failed to load rejected reasons.");
			e.printStackTrace();
		}

		System.out.println("Rejected reasons loaded: " + list.size());

		return list;
	}

	// Fetch one rejection reason by ID.
	@Override
	public RejectedReason findById(String rejectedReasonId) {

		if (rejectedReasonId == null || rejectedReasonId.trim().isEmpty()) {

			return null;
		}

		String sql = "SELECT rejected_reason_id, rejected_reason_code, "+ "rejected_reason_name, rejected_reason_description " + "FROM rejected_reasons "
				+ "WHERE rejected_reason_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, rejectedReasonId.trim());

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {
					return mapResultSet(rs);
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to find rejected reason: " + rejectedReasonId);

			e.printStackTrace();
		}

		return null;
	}

	// Map database row to entity.
	private RejectedReason mapResultSet(ResultSet rs) throws SQLException {

		RejectedReason reason = new RejectedReason();

		reason.setRejectedReasonId(rs.getString("rejected_reason_id"));

		reason.setRejectedReasonCode(rs.getString("rejected_reason_code"));

		reason.setRejectedReasonName(rs.getString("rejected_reason_name"));

		reason.setRejectedReasonDescription(rs.getString("rejected_reason_description"));

		return reason;
	}
}
package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.dao.SendBackReasonDAO;
import com.iispl.cts.entity.SendBackReason;
import com.iispl.cts.common.config.DBConnection; 

public class SendBackReasonDAOImpl implements SendBackReasonDAO {

    private static final String SQL_SELECT_ALL = 
        "SELECT reason_id, reason_code, reason_name, reason_description " +
        "FROM send_back_reason ORDER BY reason_id ASC";

    @Override
    public List<SendBackReason> getAllSendBackReasons() {
        List<SendBackReason> reasons = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SendBackReason r = new SendBackReason();
                r.setReasonId(rs.getInt("reason_id"));
                r.setReasonCode(rs.getString("reason_code"));
                r.setReasonName(rs.getString("reason_name"));
                r.setReasonDescription(rs.getString("reason_description"));
                reasons.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reasons;
    }
}
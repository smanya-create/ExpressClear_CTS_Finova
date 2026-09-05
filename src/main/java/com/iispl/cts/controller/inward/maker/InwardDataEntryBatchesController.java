package com.iispl.cts.controller.inward.maker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dto.DataEntryBatchItemDTO;

public class InwardDataEntryBatchesController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire
    private Listbox lbxDataEntryBatches;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        loadBatches();
    }

    @Listen("onClick = #btnRefresh")
    public void onRefresh() {
        loadBatches();
    }

    private void loadBatches() {
        List<DataEntryBatchItemDTO> list = fetchEligibleBatches();
        lbxDataEntryBatches.setModel(new ListModelList<>(list));
    }

    private List<DataEntryBatchItemDTO> fetchEligibleBatches() {
        List<DataEntryBatchItemDTO> batches = new ArrayList<>();

        // 1. Batch must be Validated
        // 2. Batch must have ZERO items remaining in MICR_REPAIR_REQUIRED
        String sql = "SELECT " +
                     "    b.inward_batch_id, " +
                     "    b.actual_cheque_count, " +
                     "    b.actual_total_amount, " +
                     "    b.batch_status, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('SCANNED', 'DEC_PENDING', 'MICR_REPAIRED') THEN 1 END) AS pending_cheques " +
                     "FROM inward_batch b " +
                     "LEFT JOIN inward_cheque c ON b.inward_batch_id = c.inward_batch_id " +
                     "WHERE b.batch_status = 'Validated' " +
                     "  AND NOT EXISTS ( " +
                     "      SELECT 1 FROM inward_cheque ic " +
                     "      WHERE ic.inward_batch_id = b.inward_batch_id " +
                     "        AND ic.cheque_status = 'MICR_REPAIR_REQUIRED' " +
                     "  ) " +
                     "GROUP BY b.inward_batch_id, b.actual_cheque_count, b.actual_total_amount, b.batch_status " +
                     "ORDER BY b.inward_batch_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DataEntryBatchItemDTO dto = new DataEntryBatchItemDTO();
                dto.setBatchId(rs.getString("inward_batch_id"));
                dto.setTotalCheques(rs.getInt("actual_cheque_count"));
                dto.setTotalAmount(rs.getBigDecimal("actual_total_amount"));
                dto.setBatchStatus(rs.getString("batch_status"));
                dto.setPendingCheques(rs.getInt("pending_cheques"));
                batches.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Messagebox.show("Database error: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
        return batches;
    }

    @Listen("onProcessBatch = #lbxDataEntryBatches")
    public void onProcessBatch(ForwardEvent event) {
        DataEntryBatchItemDTO batch = (DataEntryBatchItemDTO) event.getData();

        // If batch is completed, promote to Checker
        if (batch.getPendingCheques() == 0) {
            submitBatchToChecker(batch.getBatchId());
            return;
        }

        // Otherwise, send batchId to Data Entry workspace
        Sessions.getCurrent().setAttribute("ACTIVE_INWARD_BATCH_ID", batch.getBatchId());

        Include mainInclude = (Include) Path.getComponent("/mainLayout/contentInclude");
        if (mainInclude != null) {
            mainInclude.setSrc(null);
            mainInclude.setSrc("inward/maker/data-entry/data-entry.zul");
        }
    }

    private void submitBatchToChecker(String batchId) {
        Messagebox.show("Submit batch " + batchId + " to Inward Checker?", 
            "Submit Confirmation", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, evt -> {
                if (Messagebox.ON_YES.equals(evt.getName())) {
                    String sql = "UPDATE inward_batch SET batch_status = 'submit_to_ichecker' WHERE inward_batch_id = ?";
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, batchId);
                        ps.executeUpdate();
                        Messagebox.show("Batch submitted to checker.", "Success", Messagebox.OK, Messagebox.INFORMATION);
                        loadBatches();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Messagebox.show("Update failed: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
                    }
                }
            });
    }
}
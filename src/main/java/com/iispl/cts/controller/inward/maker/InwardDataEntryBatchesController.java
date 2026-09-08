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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dto.DataEntryBatchItemDTO;

public class InwardDataEntryBatchesController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Listbox lbxDataEntryBatches;
    private Button btnRefresh;
    private Textbox txtSearchBatch;
    private Button btnClearSearch;

    // Cache the full batch list for instant search/filtering
    private List<DataEntryBatchItemDTO> allBatches = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initListRenderer();
        loadBatches();
    }

    public void onClick$btnRefresh() {
        loadBatches();
    }

    public void onChange$txtSearchBatch() {
        applyFilter();
    }

    public void onClick$btnClearSearch() {
        if (txtSearchBatch != null) {
            txtSearchBatch.setValue("");
        }
        applyFilter();
    }

    private void initListRenderer() {
        if (lbxDataEntryBatches == null) return;

        lbxDataEntryBatches.setItemRenderer(new ListitemRenderer<DataEntryBatchItemDTO>() {
            @Override
            public void render(Listitem item, DataEntryBatchItemDTO batch, int index) throws Exception {
                item.setValue(batch);
                item.setStyle("border-bottom: 1px solid #f1f5f9;");

                // 1. Batch ID
                Listcell cellBatchId = new Listcell(batch.getBatchId());
                cellBatchId.setStyle("font-weight: 700; color: #1e293b;");
                cellBatchId.setParent(item);

                // 2. Total Items
                Listcell cellTotal = new Listcell(String.valueOf(batch.getTotalCheques()));
                cellTotal.setStyle("font-weight: 500;");
                cellTotal.setParent(item);

                // 3. Pending Items
                Listcell cellPending = new Listcell(String.valueOf(batch.getPendingCheques()));
                cellPending.setStyle("color: #0284c7; font-weight: 600;");
                cellPending.setParent(item);

                // 4. Batch Total Amount
                Listcell cellAmount = new Listcell(batch.getFormattedAmount());
                cellAmount.setStyle("font-family: monospace; font-size: 13px; font-weight: 600;");
                cellAmount.setParent(item);

                // 5. Dynamic Operational Status Badge (Replaces static "PROCESSING")
                Listcell cellStatus = new Listcell();
                Label lblStatus = new Label(batch.getDisplayStatus());
                lblStatus.setStyle(batch.getStatusBadgeStyle());
                lblStatus.setParent(cellStatus);
                cellStatus.setParent(item);

                // 6. Action Button
                Listcell cellAction = new Listcell();
                Button btnAction = new Button(batch.getActionLabel());
                btnAction.setSclass(batch.getActionButtonClass());
                btnAction.addEventListener(Events.ON_CLICK, (Event e) -> processBatch(batch));
                btnAction.setParent(cellAction);
                cellAction.setParent(item);
            }
        });
    }

    private void loadBatches() {
        this.allBatches = fetchEligibleBatches();
        applyFilter();
    }

    private void applyFilter() {
        if (lbxDataEntryBatches == null) return;

        String query = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
                ? txtSearchBatch.getValue().trim().toLowerCase()
                : "";

        if (query.isEmpty()) {
            lbxDataEntryBatches.setModel(new ListModelList<>(allBatches));
            return;
        }

        List<DataEntryBatchItemDTO> filtered = new ArrayList<>();
        for (DataEntryBatchItemDTO item : allBatches) {
            boolean matchesId = item.getBatchId() != null && item.getBatchId().toLowerCase().contains(query);
            boolean matchesStatus = item.getDisplayStatus() != null && item.getDisplayStatus().toLowerCase().contains(query);

            if (matchesId || matchesStatus) {
                filtered.add(item);
            }
        }
        lbxDataEntryBatches.setModel(new ListModelList<>(filtered));
    }

    private List<DataEntryBatchItemDTO> fetchEligibleBatches() {

        List<DataEntryBatchItemDTO> batches = new ArrayList<>();

        String sql =
                "SELECT "
              + "    b.inward_batch_id, "
              + "    b.actual_cheque_count, "
              + "    b.actual_total_amount, "
              + "    b.batch_status, "

              + "    COUNT(CASE "
              + "        WHEN c.cheque_status IN ( "
              + "            'DATA_ENTRY_PENDING', "
              + "            'DATA_ENTRY_IN_PROGRESS', "
              + "            'SEND_BACK_TO_MAKER_DATA_ENTRY' "
              + "        ) "
              + "        THEN 1 "
              + "    END) AS pending_cheques "

              + "FROM inward_batch b "

              + "LEFT JOIN inward_cheque c "
              + "    ON b.inward_batch_id = c.inward_batch_id "

              + "WHERE ( "
              + "       b.batch_status = 'PROCESSING' "
              + "       OR EXISTS ( "
              + "           SELECT 1 "
              + "           FROM inward_cheque ic "
              + "           WHERE ic.inward_batch_id = b.inward_batch_id "
              + "           AND ic.cheque_status = 'SEND_BACK_TO_MAKER_DATA_ENTRY' "
              + "       ) "
              + ") "

              + "AND EXISTS ( "
              + "    SELECT 1 "
              + "    FROM inward_cheque ic "
              + "    WHERE ic.inward_batch_id = b.inward_batch_id "
              + "    AND ic.cheque_status IN ( "
              + "        'DATA_ENTRY_PENDING', "
              + "        'DATA_ENTRY_IN_PROGRESS', "
              + "        'SEND_BACK_TO_MAKER_DATA_ENTRY' "
              + "    ) "
              + ") "

              + "GROUP BY "
              + "    b.inward_batch_id, "
              + "    b.actual_cheque_count, "
              + "    b.actual_total_amount, "
              + "    b.batch_status "

              + "ORDER BY b.inward_batch_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                DataEntryBatchItemDTO dto =
                        new DataEntryBatchItemDTO();

                dto.setBatchId(
                        rs.getString("inward_batch_id"));

                dto.setTotalCheques(
                        rs.getInt("actual_cheque_count"));

                dto.setTotalAmount(
                        rs.getBigDecimal("actual_total_amount"));

                dto.setBatchStatus(
                        rs.getString("batch_status"));

                dto.setPendingCheques(
                        rs.getInt("pending_cheques"));

                batches.add(dto);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            Messagebox.show(
                    "Database error: " + e.getMessage(),
                    "Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }

        return batches;
    }
    
    
    private void processBatch(DataEntryBatchItemDTO batch) {
        if (batch.getPendingCheques() == 0) {
            submitBatchToChecker(batch.getBatchId());
            return;
        }

        Sessions.getCurrent().setAttribute("ACTIVE_INWARD_BATCH_ID", batch.getBatchId());

        Include mainInclude = null;
        try {
            mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
        } catch (Exception ignored) {}

        if (mainInclude == null && self != null && self.getDesktop() != null) {
            for (org.zkoss.zk.ui.Page p : self.getDesktop().getPages()) {
                Component comp = p.getFellowIfAny("mainContentArea", true);
                if (comp instanceof Include) {
                    mainInclude = (Include) comp;
                    break;
                }
            }
        }

        if (mainInclude != null) {
            mainInclude.setSrc(null);
            mainInclude.setSrc("/inward/maker/data-entry/data-entry.zul");

            Component root = (self.getPage() != null) ? self.getPage().getFirstRoot() : null;
            if (root != null) {
                Label lblSubtitle = (Label) root.getFellowIfAny("lblPageSubtitle", true);
                if (lblSubtitle != null) {
                    lblSubtitle.setValue("Data Entry Workspace");
                }
            }
        } else {
            Messagebox.show("Navigation container (mainContentArea) not found.", "Navigation Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    private void submitBatchToChecker(String batchId) {
        Messagebox.show("Submit batch " + batchId + " to Inward Checker?", 
            "Submit Confirmation", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, evt -> {
                if (Messagebox.ON_YES.equals(evt.getName())) {
                    String sql = "UPDATE inward_batch SET batch_status = 'CHECKER_PROCESSING_PENDING' WHERE inward_batch_id = ?";
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, batchId);
                        ps.executeUpdate();
                        Messagebox.show("Batch submitted to checker successfully.", "Success", Messagebox.OK, Messagebox.INFORMATION);
                        loadBatches();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Messagebox.show("Update failed: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
                    }
                }
            });
    }
}
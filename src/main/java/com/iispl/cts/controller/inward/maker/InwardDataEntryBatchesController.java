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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dto.DataEntryBatchItemDTO;
import com.iispl.cts.enums.inward.InwardBatchStatus;
import com.iispl.cts.enums.inward.InwardChequeStatus;

public class InwardDataEntryBatchesController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Grid grdDataEntryBatches;
    private Rows rowsDataEntryBatches;
    private Paging pagingDataEntry;
    private Div divDataEntryEmpty;

    private Textbox txtSearchBatch;
    private Button btnClearSearch;
    private Label lblBatchResultCount;

    private List<DataEntryBatchItemDTO> allBatches = new ArrayList<>();
    private List<DataEntryBatchItemDTO> filteredBatches = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        if (pagingDataEntry != null) {
            pagingDataEntry.addEventListener("onPaging", new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    renderPage();
                }
            });
        }

        loadBatches();
    }

    public void onChanging$txtSearchBatch(InputEvent event) {
        applyFilter(event.getValue());
    }

    public void onChange$txtSearchBatch() {
        applyFilter(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
    }

    public void onClick$btnClearSearch() {
        if (txtSearchBatch != null) {
            txtSearchBatch.setValue("");
        }
        applyFilter("");
    }

    private void loadBatches() {
        this.allBatches = fetchEligibleBatches();
        applyFilter(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
    }

    private void applyFilter(String rawQuery) {
        String query = (rawQuery != null) ? rawQuery.trim().toLowerCase() : "";

        if (query.isEmpty()) {
            this.filteredBatches = new ArrayList<>(allBatches);
        } else {
            this.filteredBatches = new ArrayList<>();
            for (DataEntryBatchItemDTO item : allBatches) {
                boolean matchesId = item.getBatchId() != null && item.getBatchId().toLowerCase().contains(query);
                boolean matchesStatus = item.getDisplayStatus() != null && item.getDisplayStatus().toLowerCase().contains(query);

                if (matchesId || matchesStatus) {
                    this.filteredBatches.add(item);
                }
            }
        }

        setupPagination();
    }

    private void setupPagination() {
        int totalSize = filteredBatches.size();

        if (lblBatchResultCount != null) {
            lblBatchResultCount.setValue(totalSize + (totalSize == 1 ? " Batch" : " Batches"));
        }

        if (totalSize == 0) {
            if (grdDataEntryBatches != null) grdDataEntryBatches.setVisible(false);
            if (pagingDataEntry != null) pagingDataEntry.setVisible(false);
            if (divDataEntryEmpty != null) divDataEntryEmpty.setVisible(true);
            return;
        }

        if (grdDataEntryBatches != null) grdDataEntryBatches.setVisible(true);
        if (divDataEntryEmpty != null) divDataEntryEmpty.setVisible(false);

        if (pagingDataEntry != null) {
            pagingDataEntry.setTotalSize(totalSize);
            pagingDataEntry.setActivePage(0);
            pagingDataEntry.setVisible(totalSize > pagingDataEntry.getPageSize());
        }

        renderPage();
    }

    private void renderPage() {
        if (rowsDataEntryBatches == null) return;
        rowsDataEntryBatches.getChildren().clear();

        int pageSize = (pagingDataEntry != null) ? pagingDataEntry.getPageSize() : 10;
        int activePage = (pagingDataEntry != null) ? pagingDataEntry.getActivePage() : 0;

        int startIndex = activePage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredBatches.size());

        for (int i = startIndex; i < endIndex; i++) {
            DataEntryBatchItemDTO batch = filteredBatches.get(i);
            if (batch != null) {
                createBatchRow(batch);
            }
        }
    }

    private void createBatchRow(DataEntryBatchItemDTO batch) {
        Row row = new Row();

        // 1. Batch ID Link (Identical to MICR Repair)
        Label lblBatchId = new Label(batch.getBatchId());
        lblBatchId.setSclass("micr-repair-batch-id");
        lblBatchId.addEventListener("onClick", event -> processBatch(batch));

        // 2. Total Items
        Label lblTotal = new Label(String.valueOf(batch.getTotalCheques()));
        lblTotal.setSclass("micr-repair-count-text");

        // 3. Pending Items (Highlighted amber)
        Label lblPending = new Label(String.valueOf(batch.getPendingCheques()));
        lblPending.setSclass("micr-repair-pending-count");

        // 4. Total Amount
        Label lblAmount = new Label(batch.getFormattedAmount());
        lblAmount.setSclass("micr-repair-cell-text");
        lblAmount.setStyle("font-weight: 700; color: #0f172a;");

        // 5. Status Badge (Exact Golden Pill from MICR Repair)
        Label lblStatus = new Label("PENDING_MAKER_PROCESS");
        lblStatus.setSclass("micr-repair-status");

        // 6. Action Button (Exact 'OPEN' Navy Button from MICR Repair)
        Button btnAction = new Button("OPEN");
        btnAction.setSclass("btn-action-repair");
        btnAction.addEventListener("onClick", event -> processBatch(batch));

        row.appendChild(lblBatchId);
        row.appendChild(lblTotal);
        row.appendChild(lblPending);
        row.appendChild(lblAmount);
        row.appendChild(lblStatus);
        row.appendChild(btnAction);

        rowsDataEntryBatches.appendChild(row);
    }

    private List<DataEntryBatchItemDTO> fetchEligibleBatches() {
        List<DataEntryBatchItemDTO> batches = new ArrayList<>();

        String sql = "SELECT " +
                     "    b.inward_batch_id, " +
                     "    b.actual_cheque_count, " +
                     "    b.actual_total_amount, " +
                     "    b.batch_status, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('" 
                     + InwardChequeStatus.DATA_ENTRY_PENDING.name() + "', '" 
                     + InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER.name() + "') THEN 1 END) AS pending_cheques, " +
                     "    COUNT(CASE WHEN c.cheque_status IN ('" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER.name() + "') THEN 1 END) AS sent_back_cheques " +
                     "FROM inward_batch b " +
                     "JOIN inward_cheque c ON b.inward_batch_id = c.inward_batch_id " +
                     "WHERE EXISTS ( " +
                     "    SELECT 1 FROM inward_cheque ic_need " +
                     "    WHERE ic_need.inward_batch_id = b.inward_batch_id " +
                     "      AND ic_need.cheque_status IN ('" 
                     + InwardChequeStatus.DATA_ENTRY_PENDING.name() + "', '" 
                     + InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER.name() + "') " +
                     ") " +
                     "  AND NOT EXISTS ( " +
                     "      SELECT 1 FROM inward_cheque ic_micr " +
                     "      WHERE ic_micr.inward_batch_id = b.inward_batch_id " +
                     "        AND ic_micr.cheque_status IN ('" 
                     + InwardChequeStatus.MICR_REPAIR_PENDING.name() + "', '" 
                     + InwardChequeStatus.MICR_REPAIR_IN_PROGRESS.name() + "', '" 
                     + InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name() + "') " +
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
                
                int sentBackCount = rs.getInt("sent_back_cheques");
                String bStatus = rs.getString("batch_status");
                
                if (sentBackCount > 0) {
                    dto.setBatchStatus(InwardBatchStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name());
                } else {
                    dto.setBatchStatus(bStatus);
                }

                dto.setPendingCheques(rs.getInt("pending_cheques"));
                batches.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Messagebox.show("Database error loading batches: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }

        batches.sort((b1, b2) -> {
            boolean b1Sb = InwardBatchStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(b1.getBatchStatus());
            boolean b2Sb = InwardBatchStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(b2.getBatchStatus());
            if (b1Sb && !b2Sb) return -1;
            if (!b1Sb && b2Sb) return 1;
            return 0;
        });

        return batches;
    }

    private void processBatch(DataEntryBatchItemDTO batch) {
        Sessions.getCurrent().setAttribute("ACTIVE_INWARD_BATCH_ID", batch.getBatchId());
        Sessions.getCurrent().setAttribute("batchId", batch.getBatchId());

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
            mainInclude.setSrc("/inward/maker/data-entry/data-entry.zul?batchId=" + batch.getBatchId());

            Component root = (self.getPage() != null) ? self.getPage().getFirstRoot() : null;
            if (root != null) {
                Label lblSubtitle = (Label) root.getFellowIfAny("lblPageSubtitle", true);
                if (lblSubtitle != null) {
                    lblSubtitle.setValue("Data Entry");
                }
            }
        } else {
            Messagebox.show("Navigation container (mainContentArea) not found.", "Navigation Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}
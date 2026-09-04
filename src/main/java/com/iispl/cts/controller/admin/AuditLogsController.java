package com.iispl.cts.controller.admin;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.event.PagingEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Cell;

import com.iispl.cts.entity.AuditLog;
import com.iispl.cts.service.AuditService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class AuditLogsController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Auto-wired components by ZUL ID match
    private Datebox dtFrom;
    private Datebox dtTo;
    private Combobox cmbModuleFilter;
    private Textbox txtSearchAudit;
    private Button btnSearch;
    private Button btnReset;
    private Label lblAuditCount;
    private Rows rowsAudit;
    private Paging auditPaging;

    private final AuditService auditService = AuditServiceImpl.getInstance();
    private static final int PAGE_SIZE = 15;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        if (auditPaging != null) {
            auditPaging.setPageSize(PAGE_SIZE);
        }

        // Default to today only (per trainer's requirement)
        resetFiltersToToday();

        // Load page 0 for today
        loadAuditPage(0);
    }

    /**
     * Sets date filters to the start and end of the current day.
     */
    private void resetFiltersToToday() {
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (dtFrom != null) dtFrom.setValue(todayDate);
        if (dtTo != null) dtTo.setValue(todayDate);

        if (cmbModuleFilter != null && cmbModuleFilter.getItemCount() > 0) {
            cmbModuleFilter.setSelectedIndex(0);
        }

        if (txtSearchAudit != null) {
            txtSearchAudit.setValue("");
        }
    }

    /**
     * Queries database by limit and offset, updates paging bar and renders grid.
     */
    private void loadAuditPage(int activePageIndex) {
        Date from = dtFrom != null ? dtFrom.getValue() : null;
        Date to = dtTo != null ? dtTo.getValue() : null;
        String mod = (cmbModuleFilter != null && cmbModuleFilter.getSelectedItem() != null)
                     ? cmbModuleFilter.getSelectedItem().getValue() : "ALL";
        String q = (txtSearchAudit != null && txtSearchAudit.getValue() != null) 
                   ? txtSearchAudit.getValue().trim() : "";

        // 1. Fetch total count for paging bar calculation
        int totalRecords = auditService.countAuditLogs(from, to, mod, null, q);

        if (auditPaging != null) {
            auditPaging.setTotalSize(totalRecords);
            auditPaging.setActivePage(activePageIndex);
        }

        if (lblAuditCount != null) {
            lblAuditCount.setValue(totalRecords + " records found");
        }

        // 2. Fetch only the active page records from database
        int offset = activePageIndex * PAGE_SIZE;
        List<AuditLog> logs = auditService.searchAuditLogs(from, to, mod, null, q, offset, PAGE_SIZE);

        if (rowsAudit == null) return;
        rowsAudit.getChildren().clear();

        if (logs.isEmpty()) {
            Row emptyRow = new Row();
            
            Cell cell = new Cell();
            cell.setColspan(7); // Spans all 7 columns of the grid
            cell.setStyle("text-align: center; padding: 24px;");

            Label emptyLbl = new Label("No audit records found for the selected criteria.");
            emptyLbl.setStyle("color: #94a3b8; font-style: italic; font-size: 13px;");
            
            cell.appendChild(emptyLbl);
            emptyRow.appendChild(cell);
            rowsAudit.appendChild(emptyRow);
            return;
        }

        for (AuditLog log : logs) {
            Row row = new Row();
            row.setStyle("border-bottom: 1px solid #f1f5f9; height: 48px;");

            // Timestamp
            Label lblTime = new Label(log.getTimestamp() != null ? df.format(log.getTimestamp()) : "-");
            lblTime.setStyle("font-size: 12px; color: #64748b;");
            row.appendChild(lblTime);

            // User / Role
            Label lblUser = new Label((log.getUsername() != null ? log.getUsername() : log.getUserId()) + " (" + log.getRoleName() + ")");
            lblUser.setStyle("font-size: 13px; font-weight: 600; color: #1e293b;");
            row.appendChild(lblUser);

            // Module Badge
            Label lblMod = new Label(log.getModule());
            lblMod.setStyle("font-size: 11px; background: #e0f2fe; color: #0369a1; padding: 3px 8px; border-radius: 4px; font-weight: 600;");
            row.appendChild(lblMod);

            // Action
            Label lblAction = new Label(log.getAction());
            lblAction.setStyle("font-size: 12px; font-weight: 600; color: #334155;");
            row.appendChild(lblAction);

            // Details
            Label lblDetails = new Label(log.getDetails() != null ? log.getDetails() : "-");
            lblDetails.setStyle("font-size: 12px; color: #475569;");
            row.appendChild(lblDetails);

            // IP Address
            Label lblIp = new Label(log.getIpAddress() != null ? log.getIpAddress() : "-");
            lblIp.setStyle("font-size: 12px; color: #64748b;");
            row.appendChild(lblIp);

            // Status Badge
            Label lblStatus = new Label(log.getStatus() != null ? log.getStatus() : "SUCCESS");
            if ("SUCCESS".equalsIgnoreCase(log.getStatus())) {
                lblStatus.setStyle("background: #dcfce7; color: #15803d; padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 700;");
            } else {
                lblStatus.setStyle("background: #fee2e2; color: #b91c1c; padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 700;");
            }
            row.appendChild(lblStatus);

            rowsAudit.appendChild(row);
        }
    }

    // Triggered on Search button click
    public void onClick$btnSearch(Event event) { 
        loadAuditPage(0); 
    }

    // Triggered when hitting Enter in search textbox
    public void onOK$txtSearchAudit(Event event) { 
        loadAuditPage(0); 
    }

    // Reset button resets to Today's date range and loads Page 0
    public void onClick$btnReset(Event event) {
        resetFiltersToToday();
        loadAuditPage(0);
    }

    // Navigation event from the ZK <paging id="auditPaging" ... /> component
    public void onPaging$auditPaging(PagingEvent event) {
        loadAuditPage(event.getActivePage());
    }
}
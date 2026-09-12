package com.iispl.cts.controller.admin;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.entity.AuditLog;
import com.iispl.cts.service.AuditService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class AuditLogsController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Filter controls
    private Datebox dtFrom;
    private Datebox dtTo;
    private Combobox cmbModuleFilter;
    private Textbox txtSearchAudit;
    private Button btnSearch;
    private Button btnReset;
    private Label lblAuditCount;
    private Rows rowsAudit;

    // Custom Pagination Toolbar Controls
    private Button btnFirstPage;
    private Button btnPrevPage;
    private Intbox ibCurrentPage;
    private Label lblTotalPages;
    private Button btnNextPage;
    private Button btnLastPage;

    // Pagination State
    private static final int PAGE_SIZE = 10;
    private int activePageIndex = 0;
    private int totalPages = 1;
    private int totalRecords = 0;

    private final AuditService auditService = AuditServiceImpl.getInstance();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
    	if(!SecurityUtil.checkAccess(null)) {
    		return;
    	}
        super.doAfterCompose(comp);

        resetFiltersToToday();
        loadAuditPage(0);
    }

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

    private void loadAuditPage(int pageIndex) {
        Date from = dtFrom != null ? dtFrom.getValue() : null;
        Date to = dtTo != null ? dtTo.getValue() : null;
        String mod = (cmbModuleFilter != null && cmbModuleFilter.getSelectedItem() != null)
                     ? cmbModuleFilter.getSelectedItem().getValue() : "ALL";
        String q = (txtSearchAudit != null && txtSearchAudit.getValue() != null) 
                   ? txtSearchAudit.getValue().trim() : "";

        // 1. Fetch total count from DB
        this.totalRecords = auditService.countAuditLogs(from, to, mod, null, q);
        this.totalPages = (int) Math.ceil((double) this.totalRecords / PAGE_SIZE);
        if (this.totalPages < 1) {
            this.totalPages = 1;
        }

        // Validate index boundaries
        if (pageIndex >= this.totalPages) {
            pageIndex = this.totalPages - 1;
        }
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        this.activePageIndex = pageIndex;

        // 2. Update pagination toolbar display
        if (lblAuditCount != null) {
            lblAuditCount.setValue(this.totalRecords + " records found");
        }
        if (ibCurrentPage != null) {
            ibCurrentPage.setValue(this.activePageIndex + 1);
        }
        if (lblTotalPages != null) {
            lblTotalPages.setValue("/ " + this.totalPages);
        }

        boolean isFirst = (this.activePageIndex <= 0);
        boolean isLast = (this.activePageIndex >= this.totalPages - 1);

        if (btnFirstPage != null) btnFirstPage.setDisabled(isFirst);
        if (btnPrevPage != null) btnPrevPage.setDisabled(isFirst);
        if (btnNextPage != null) btnNextPage.setDisabled(isLast);
        if (btnLastPage != null) btnLastPage.setDisabled(isLast);

        // 3. Query DB with LIMIT & OFFSET
        int offset = this.activePageIndex * PAGE_SIZE;
        List<AuditLog> logs = auditService.searchAuditLogs(from, to, mod, null, q, offset, PAGE_SIZE);

        renderAuditRows(logs);
    }

    private void renderAuditRows(List<AuditLog> logs) {
        if (rowsAudit == null) return;
        rowsAudit.getChildren().clear();

        if (logs == null || logs.isEmpty()) {
            Row emptyRow = new Row();
            Cell cell = new Cell();
            cell.setColspan(7);
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

            // 1. Timestamp (Centered)
            Label lblTime = new Label(log.getTimestamp() != null ? df.format(log.getTimestamp()) : "-");
            lblTime.setStyle("font-size: 12px; color: #64748b; display: block; text-align: center;");
            row.appendChild(lblTime);

            // 2. User / Role (Left aligned)
            Label lblUser = new Label((log.getUsername() != null ? log.getUsername() : log.getUserId()) 
                                      + " (" + (log.getRoleName() != null ? log.getRoleName() : "-") + ")");
            lblUser.setStyle("font-size: 13px; font-weight: 600; color: #1e293b; display: block; text-align: left; padding-left: 6px;");
            row.appendChild(lblUser);

            // 3. Module Badge (Centered)
            Label lblMod = new Label(log.getModule());
            lblMod.setStyle("font-size: 11px; background: #e0f2fe; color: #0369a1; padding: 3px 10px; border-radius: 4px; font-weight: 600; display: table; margin: 0 auto;");
            row.appendChild(lblMod);

            // 4. Action (Left aligned)
            Label lblAction = new Label(log.getAction());
            lblAction.setStyle("font-size: 12px; font-weight: 600; color: #334155; display: block; text-align: left; padding-left: 6px;");
            row.appendChild(lblAction);

            // 5. Details (Left aligned)
            Label lblDetails = new Label(log.getDetails() != null ? log.getDetails() : "-");
            lblDetails.setStyle("font-size: 12px; color: #475569; display: block; text-align: left; padding-left: 6px;");
            row.appendChild(lblDetails);

            // 6. IP Address (Centered)
            Label lblIp = new Label(log.getIpAddress() != null ? log.getIpAddress() : "-");
            lblIp.setStyle("font-size: 12px; color: #64748b; display: block; text-align: center;");
            row.appendChild(lblIp);

            // 7. Status Badge (Centered with scrollbar padding)
            Label lblStatus = new Label(log.getStatus() != null ? log.getStatus() : "SUCCESS");
            String baseBadgeStyle = "display: table; margin: 0 auto; padding: 3px 12px; border-radius: 12px; font-size: 11px; font-weight: 700; white-space: nowrap; line-height: 1.2; text-align: center;";

            if ("SUCCESS".equalsIgnoreCase(log.getStatus())) {
                lblStatus.setStyle(baseBadgeStyle + " background: #dcfce7; color: #15803d;");
            } else {
                lblStatus.setStyle(baseBadgeStyle + " background: #fee2e2; color: #b91c1c;");
            }

            row.appendChild(lblStatus);

            rowsAudit.appendChild(row);
        }
    }

    // Filter Actions
    public void onClick$btnSearch(Event event) { 
        loadAuditPage(0); 
    }

    public void onOK$txtSearchAudit(Event event) { 
        loadAuditPage(0); 
    }

    public void onClick$btnReset(Event event) {
        resetFiltersToToday();
        loadAuditPage(0);
    }

    // Pagination Toolbar Actions
    public void onClick$btnFirstPage(Event event) {
        if (activePageIndex > 0) {
            loadAuditPage(0);
        }
    }

    public void onClick$btnPrevPage(Event event) {
        if (activePageIndex > 0) {
            loadAuditPage(activePageIndex - 1);
        }
    }

    public void onClick$btnNextPage(Event event) {
        if (activePageIndex < totalPages - 1) {
            loadAuditPage(activePageIndex + 1);
        }
    }

    public void onClick$btnLastPage(Event event) {
        if (activePageIndex < totalPages - 1) {
            loadAuditPage(totalPages - 1);
        }
    }

    public void onChange$ibCurrentPage(Event event) {
        Integer target = ibCurrentPage.getValue();
        if (target == null || target < 1) {
            target = 1;
        } else if (target > totalPages) {
            target = totalPages;
        }
        loadAuditPage(target - 1);
    }

    public void onOK$ibCurrentPage(Event event) {
        onChange$ibCurrentPage(event);
    }
}
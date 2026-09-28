package com.iispl.cts.controller.admin;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.util.ClearingTimeMock;
import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.dto.AuditSearchResult;
import com.iispl.cts.entity.AuditLog;
import com.iispl.cts.service.AuditService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class AuditLogsController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Filters and search controls
    private Datebox dtFrom;
    private Datebox dtTo;
    private Textbox txtSearchAudit;
    private Button btnSearch;
    private Button btnReset;
    private Label lblAuditCount;
    private Rows rowsAudit;

    // Pagination controls
    private Button btnFirstPage;
    private Button btnPrevPage;
    private Intbox ibCurrentPage;
    private Label lblTotalPages;
    private Button btnNextPage;
    private Button btnLastPage;

    // Page state
    private static final int PAGE_SIZE = 10;
    private int activePageIndex = 0;
    private int totalPages = 1;
    private int totalRecords = 0;

    private final AuditService auditService = AuditServiceImpl.getInstance();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        if (!SecurityUtil.checkAccess(null)) {
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

        if (txtSearchAudit != null) {
            txtSearchAudit.setValue("");
        }
    }

    private void loadAuditPage(int pageIndex) {
        Date from = dtFrom != null ? dtFrom.getValue() : null;
        Date to = dtTo != null ? dtTo.getValue() : null;
        String q = (txtSearchAudit != null && txtSearchAudit.getValue() != null) 
                   ? txtSearchAudit.getValue().trim() : "";

        int targetIndex = Math.max(0, pageIndex);
        int offset = targetIndex * PAGE_SIZE;

        // Dispatches search and count query simultaneously
        AuditSearchResult result = auditService.searchAuditLogsConcurrently(
                from, to, null, null, q, offset, PAGE_SIZE
        );

        this.totalRecords = result.getTotalCount();
        this.totalPages = (int) Math.ceil((double) this.totalRecords / PAGE_SIZE);
        if (this.totalPages < 1) {
            this.totalPages = 1;
        }

        // Adjust if current page exceeds boundary after applying new filter
        if (targetIndex >= this.totalPages) {
            targetIndex = this.totalPages - 1;
        }
        this.activePageIndex = targetIndex;

        // Update toolbar
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

        renderAuditRows(result.getLogs());
    }

    private void renderAuditRows(List<AuditLog> logs) {
        if (rowsAudit == null) return;
        rowsAudit.getChildren().clear();

        if (logs == null || logs.isEmpty()) {
            Row emptyRow = new Row();
            Cell cell = new Cell();
            cell.setColspan(6);
            cell.setStyle("text-align: center; padding: 24px;");

            Label emptyLbl = new Label("No audit records found for the selected criteria.");
            emptyLbl.setStyle("color: #94a3b8; font-style: italic; font-size: 13px;");
            
            cell.appendChild(emptyLbl);
            emptyRow.appendChild(cell);
            rowsAudit.appendChild(emptyRow);
            return;
        }

        Object sessionDateObj = Sessions.getCurrent().getAttribute("CTS_CLEARING_DATE");
        LocalDate clearingDate;
        if (sessionDateObj instanceof LocalDate) {
            clearingDate = (LocalDate) sessionDateObj;
        } else if (sessionDateObj instanceof java.sql.Date) {
            clearingDate = ((java.sql.Date) sessionDateObj).toLocalDate();
        } else {
            clearingDate = LocalDate.now();
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (AuditLog log : logs) {
            Row row = new Row();
            row.setStyle("border-bottom: 1px solid #f1f5f9; min-height: 48px;");

            // Timestamp column
            String displayTime;
            if (log.getTimestamp() != null) {
                LocalTime logTime = log.getTimestamp().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalTime();
                displayTime = clearingDate.format(dateFmt) + " " + logTime.format(timeFmt);
            } else {
                displayTime = clearingDate.format(dateFmt) + " " + ClearingTimeMock.getCurrentTime().format(timeFmt);
            }

            Label lblTime = new Label(displayTime);
            lblTime.setStyle("font-size: 12px; color: #64748b; display: block; text-align: center;");
            row.appendChild(lblTime);

            // User column
            String cleanUser = log.getUsername() != null && !log.getUsername().trim().isEmpty() 
                             ? log.getUsername().trim() 
                             : (log.getUserId() != null ? log.getUserId().trim() : "-");

            Label lblUser = new Label(cleanUser);
            lblUser.setStyle("font-size: 13px; font-weight: 600; color: #1e293b; display: block; text-align: center;");
            row.appendChild(lblUser);

            // Action column
            Label lblAction = new Label(log.getAction() != null ? log.getAction() : "-");
            lblAction.setStyle("font-size: 12px; font-weight: 600; color: #334155; display: block; text-align: center;");
            row.appendChild(lblAction);

            // Details column
            Component detailsCell = createFormattedDetailsCell(log.getDetails());
            row.appendChild(detailsCell);

            // IP column
            Label lblIp = new Label(log.getIpAddress() != null ? log.getIpAddress() : "-");
            lblIp.setStyle("font-size: 12px; color: #64748b; display: block; text-align: center;");
            row.appendChild(lblIp);

            // Status badge column
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

    private Component createFormattedDetailsCell(String rawDetails) {
        org.zkoss.zul.Div container = new org.zkoss.zul.Div();
        container.setStyle("display: flex; align-items: center; flex-wrap: wrap; gap: 6px; padding: 4px 12px;");

        if (rawDetails == null || rawDetails.trim().isEmpty() || "-".equals(rawDetails.trim())) {
            Label lbl = new Label("-");
            lbl.setStyle("color: #94a3b8; font-size: 12px;");
            container.appendChild(lbl);
            return container;
        }

        // Parse key-value tokens separated by pipe
        if (rawDetails.contains("|")) {
            String[] parts = rawDetails.split("\\|");
            for (int i = 0; i < parts.length; i++) {
                String segment = parts[i].trim();
                
                if (segment.startsWith("Updated user:")) {
                    Label lblUser = new Label(segment);
                    lblUser.setStyle("font-size: 12px; font-weight: 400; color: #475569; margin-right: 4px;");
                    container.appendChild(lblUser);
                } else {
                    Label pill = new Label(segment.replace("->", "→"));
                    pill.setStyle("font-size: 11px; font-weight: 500; color: #334155; background: #f1f5f9; "
                            + "border: 1px solid #e2e8f0; padding: 2px 8px; border-radius: 4px; white-space: nowrap;");
                    container.appendChild(pill);
                }
            }
        } else {
            Label lbl = new Label(rawDetails);
            lbl.setStyle("font-size: 12px; color: #475569; line-height: 1.4;");
            container.appendChild(lbl);
        }

        return container;
    }

    // Filter event listeners
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

    // Pagination event listeners
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
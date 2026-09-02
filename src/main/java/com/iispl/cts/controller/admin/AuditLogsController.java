package com.iispl.cts.controller.admin;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
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

    private Datebox dtFrom;
    private Datebox dtTo;
    private Combobox cmbModuleFilter;
    private Textbox txtSearchAudit;
    private Button btnSearch;
    private Button btnReset;
    private Label lblAuditCount;
    private Rows rowsAudit;

    private final AuditService auditService = AuditServiceImpl.getInstance();
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        
        // Default to today
        if (dtFrom != null) dtFrom.setValue(new Date(System.currentTimeMillis() - (7L * 24 * 3600 * 1000))); // 7 days back
        if (dtTo != null) dtTo.setValue(new Date());

        if (cmbModuleFilter != null && cmbModuleFilter.getItemCount() > 0) {
            cmbModuleFilter.setSelectedIndex(0);
        }

        loadAuditLogs();
    }

    private void loadAuditLogs() {
        Date from = dtFrom != null ? dtFrom.getValue() : null;
        Date to = dtTo != null ? dtTo.getValue() : null;
        String mod = (cmbModuleFilter != null && cmbModuleFilter.getSelectedItem() != null)
                     ? cmbModuleFilter.getSelectedItem().getValue() : "ALL";
        String q = (txtSearchAudit != null && txtSearchAudit.getValue() != null) 
                   ? txtSearchAudit.getValue().trim() : "";

        List<AuditLog> logs = auditService.searchAuditLogs(from, to, mod, null, q);

        if (lblAuditCount != null) {
            lblAuditCount.setValue(logs.size() + " records found");
        }

        if (rowsAudit == null) return;
        rowsAudit.getChildren().clear();

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

    public void onClick$btnSearch(Event event) { loadAuditLogs(); }
    public void onOK$txtSearchAudit(Event event) { loadAuditLogs(); }

    public void onClick$btnReset(Event event) {
        if (dtFrom != null) dtFrom.setValue(new Date(System.currentTimeMillis() - (7L * 24 * 3600 * 1000)));
        if (dtTo != null) dtTo.setValue(new Date());
        if (cmbModuleFilter != null) cmbModuleFilter.setSelectedIndex(0);
        if (txtSearchAudit != null) txtSearchAudit.setValue("");
        loadAuditLogs();
    }
}

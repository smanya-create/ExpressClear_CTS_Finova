package com.iispl.cts.controller.common;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

public class SidebarController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Label lblPortalTitle;
    private Div divAdminMenu;
    private Div divMakerMenu;
    private Div divCheckerMenu;
    private Div divInwardMakerMenu;
    private Div divInwardCheckerMenu;

    private A navDashboard;
    private A navUpload;
    private A navQueue;
    private A navAudit;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // 1. Retrieve role from Session directly (persists through refresh)
        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
        
        // 2. Fallback check from component arguments if session attribute is empty
        if (role == null || role.trim().isEmpty()) {
            role = (String) comp.getAttribute("role");
        }

        // 3. Fallback default role for testing if unassigned
        if (role == null || role.trim().isEmpty()) {
            role = "OUTWARD_MAKER";
            Sessions.getCurrent().setAttribute("USER_ROLE", role);
        }

        // Render menu based on verified role
        renderSidebarMenu(role);
        
        // Highlight active tab passed via attribute
        String activeTab = (String) comp.getAttribute("activeTab");
        highlightActiveTab(activeTab);
    }

    private void renderSidebarMenu(String role) {
        if (lblPortalTitle != null) {
            lblPortalTitle.setValue(role.replace("_", " ") + " PORTAL");
        }

        // Toggle role section visibility safely
        if (divAdminMenu != null) divAdminMenu.setVisible("ADMIN".equalsIgnoreCase(role));
        if (divMakerMenu != null) divMakerMenu.setVisible("OUTWARD_MAKER".equalsIgnoreCase(role));
        if (divCheckerMenu != null) divCheckerMenu.setVisible("OUTWARD_CHECKER".equalsIgnoreCase(role));
        if (divInwardMakerMenu != null) divInwardMakerMenu.setVisible("INWARD_MAKER".equalsIgnoreCase(role));
        if (divInwardCheckerMenu != null) divInwardCheckerMenu.setVisible("INWARD_CHECKER".equalsIgnoreCase(role));
    }

    private void highlightActiveTab(String activeTab) {
        if (activeTab == null) return;

        resetTabStyles();

        switch (activeTab.toLowerCase()) {
            case "dashboard":
            case "inward-dashboard":
                if (navDashboard != null) navDashboard.setSclass("nav-item active");
                break;
            case "upload":
            case "batch-intake":
                if (navUpload != null) navUpload.setSclass("nav-item active");
                break;
            case "queue":
            case "unprocessed":
            case "inward-unprocessed":
                if (navQueue != null) navQueue.setSclass("nav-item active");
                break;
            case "audit":
                if (navAudit != null) navAudit.setSclass("nav-item active");
                break;
        }
    }

    private void resetTabStyles() {
        if (navDashboard != null) navDashboard.setSclass("nav-item");
        if (navUpload != null) navUpload.setSclass("nav-item");
        if (navQueue != null) navQueue.setSclass("nav-item");
        if (navAudit != null) navAudit.setSclass("nav-item");
    }

    // ==================== COMMON & EXISTING HANDLERS ====================
    public void navToDashboard() {
        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
        if ("ADMIN".equalsIgnoreCase(role)) {
            Executions.sendRedirect("/admin/dashboard.zul");
        } else if ("OUTWARD_CHECKER".equalsIgnoreCase(role)) {
            Executions.sendRedirect("/outward/checker/checker-dashboard.zul");
        } else if ("INWARD_MAKER".equalsIgnoreCase(role)) {
            navToInwardDashboard();
        } else if ("INWARD_CHECKER".equalsIgnoreCase(role)) {
            navToVerification();
        } else {
            Executions.sendRedirect("/outward/maker/maker-dashboard.zul");
        }
    }

    public void navToAuditLogs() {
        Executions.sendRedirect("/admin/audit-log/audit-logs.zul");
    }

    public void navToUploadBatch() {
        Executions.sendRedirect("/outward/maker/upload-batch.zul");
    }

    public void navToQueue() {
        Executions.sendRedirect("/outward/maker/unprocessed-queue.zul");
    }

    // ==================== INWARD MAKER ROUTING ====================
    public void navToInwardDashboard() {
        Executions.sendRedirect("/inward/maker/inward-dashboard.zul");
    }

    public void navToBatchIntake() {
        Executions.sendRedirect("/inward/maker/batch-intake.zul");
    }

    public void navToInwardMicrRepair() {
        Executions.sendRedirect("/inward/maker/micr-repair.zul");
    }

    public void navToInwardDataEntry() {
        Executions.sendRedirect("/inward/maker/data-entry.zul");
    }

    public void navToMakerCompletion() {
        Executions.sendRedirect("/inward/maker/maker-completion.zul");
    }

    public void navToInwardUnprocessedQueue() {
        Executions.sendRedirect("/inward/maker/unprocessed-cheques.zul");
    }

    public void navToInwardMakerReports() {
        Executions.sendRedirect("/inward/maker/reports.zul");
    }

    // ==================== INWARD CHECKER ROUTING ====================
    public void navToVerification() {
        Executions.sendRedirect("/inward/checker/verification.zul");
    }

    public void navToRRF() {
        Executions.sendRedirect("/inward/checker/rrf.zul");
    }

    public void navToInwardCheckerReports() {
        Executions.sendRedirect("/inward/checker/reports.zul");
    }

    // ==================== LOGOUT HANDLER ====================
    public void onClickLogout() {
        String currentUser = (String) Sessions.getCurrent().getAttribute("LOGGED_USER");
        if (currentUser == null || currentUser.trim().isEmpty()) {
            currentUser = "User";
        }

        Messagebox.show("Are you sure you want to log out of Express Clear CTS?", "Confirm Logout",
            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, evt -> {
                if (Messagebox.ON_YES.equals(evt.getName())) {
                    Sessions.getCurrent().invalidate();
                    Executions.sendRedirect("/auth/login.zul");
                }
            });
    }
}
package com.iispl.cts.controller.common;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

public class SidebarController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // UI Header & Menu Containers
    private Label lblPortalTitle;
    private Div divAdminMenu;
    private Div divMakerMenu;
    private Div divCheckerMenu;
    private Div divInwardMakerMenu;
    private Div divInwardCheckerMenu;

    // Navigation Links (for active tab styling)
    private A navDashboard;
    private A navAdminUsers;
    private A navAdminRoles;
    private A navAudit;
    private A navAdminReports;

    private A navUpload;
    private A navOutwardMicr;
    private A navOutwardDataEntry;
    private A navQueue;
    private A navOutwardMakerReports;

    private A navCheckerQueue;
    private A navXmlGen;
    private A navOutwardRejected;
    private A navOutwardCheckerReports;

    private A navInwardDashboard;
    private A navBatchIntake;
    private A navInwardMicr;
    private A navInwardDataEntry;
    private A navMakerCompletion;
    private A navInwardUnprocessed;
    private A navInwardMakerReports;

    private A navInwardCheckerDashboard;
    private A navVerification;
    private A navRrf;
    private A navInwardCheckerReports;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // 1. Retrieve role from Session
        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
        if (role == null || role.trim().isEmpty()) {
            role = (String) comp.getAttribute("role");
        }
        if (role == null || role.trim().isEmpty()) {
            role = "OUTWARD_MAKER";
            Sessions.getCurrent().setAttribute("USER_ROLE", role);
        }

        // 2. Render sidebar visibility by role
        renderSidebarMenu(role);

        // 3. Set initial active tab
        String activeTab = (String) comp.getAttribute("activeTab");
        highlightActiveTab(activeTab);
    }

    private void renderSidebarMenu(String role) {
        if (lblPortalTitle != null) {
            lblPortalTitle.setValue(role.replace("_", " ") + " PORTAL");
        }

        if (divAdminMenu != null) divAdminMenu.setVisible("ADMIN".equalsIgnoreCase(role));
        if (divMakerMenu != null) divMakerMenu.setVisible("OUTWARD_MAKER".equalsIgnoreCase(role));
        if (divCheckerMenu != null) divCheckerMenu.setVisible("OUTWARD_CHECKER".equalsIgnoreCase(role));
        if (divInwardMakerMenu != null) divInwardMakerMenu.setVisible("INWARD_MAKER".equalsIgnoreCase(role));
        if (divInwardCheckerMenu != null) divInwardCheckerMenu.setVisible("INWARD_CHECKER".equalsIgnoreCase(role));
    }

    /**
     * Core AJAX View-Swapper: Loads the destination ZUL into the center area
     * without reloading the browser page or sidebar/header.
     */
    private void navigateTo(String zulPath, A targetTab, String pageSubtitle) {
        // 1. Swap Center Content Area via AJAX
        Include contentArea = (Include) Path.getComponent("//inwardMakerRootWin/mainContentArea");
        if (contentArea == null) {
            contentArea = (Include) Path.getComponent("/mainContentArea");
        }

        if (contentArea != null) {
            contentArea.setSrc(null); // Clear previous state
            contentArea.setSrc(zulPath);
        } else {
            // Fallback only if root layout is missing
            Executions.sendRedirect(zulPath);
            return;
        }

        // 2. Update Header Subtitle if header is present
        Include headerInclude = (Include) Path.getComponent("//inwardMakerRootWin/headerInclude");
        if (headerInclude != null && pageSubtitle != null) {
            headerInclude.setDynamicProperty("pageSubtitle", pageSubtitle);
            headerInclude.invalidate();
        }

        // 3. Highlight the clicked tab
        setActiveTab(targetTab);
    }

    private void setActiveTab(A targetTab) {
        resetTabStyles();
        if (targetTab != null) {
            targetTab.setSclass("nav-item active");
        }
    }

    private void resetTabStyles() {
        A[] allTabs = new A[] {
            navDashboard, navAdminUsers, navAdminRoles, navAudit, navAdminReports,
            navUpload, navOutwardMicr, navOutwardDataEntry, navQueue, navOutwardMakerReports,
            navCheckerQueue, navXmlGen, navOutwardRejected, navOutwardCheckerReports,
            navInwardDashboard, navBatchIntake, navInwardMicr, navInwardDataEntry, navMakerCompletion, navInwardUnprocessed, navInwardMakerReports,
            navInwardCheckerDashboard, navVerification, navRrf, navInwardCheckerReports
        };

        for (A tab : allTabs) {
            if (tab != null) {
                tab.setSclass("nav-item");
            }
        }
    }

    private void highlightActiveTab(String activeTab) {
        if (activeTab == null || activeTab.trim().isEmpty()) return;

        switch (activeTab.toLowerCase()) {
            case "dashboard":
            case "inward-dashboard":
                setActiveTab(navDashboard != null ? navDashboard : navInwardDashboard);
                break;
            case "users": setActiveTab(navAdminUsers); break;
            case "roles": setActiveTab(navAdminRoles); break;
            case "audit": setActiveTab(navAudit); break;
            case "admin-reports": setActiveTab(navAdminReports); break;

            case "upload": setActiveTab(navUpload); break;
            case "outward-micr":
            case "micr": setActiveTab(navOutwardMicr); break;
            case "outward-data-entry":
            case "data-entry": setActiveTab(navOutwardDataEntry); break;
            case "queue":
            case "unprocessed": setActiveTab(navQueue); break;
            case "outward-maker-reports": setActiveTab(navOutwardMakerReports); break;

            case "checker-queue": setActiveTab(navCheckerQueue); break;
            case "xml-gen": setActiveTab(navXmlGen); break;
            case "outward-rejected-cheques": setActiveTab(navOutwardRejected); break;
            case "outward-checker-reports": setActiveTab(navOutwardCheckerReports); break;

            case "batch-intake": setActiveTab(navBatchIntake); break;
            case "inward-micr": setActiveTab(navInwardMicr); break;
            case "inward-data-entry": setActiveTab(navInwardDataEntry); break;
            case "maker-completion": setActiveTab(navMakerCompletion); break;
            case "inward-unprocessed": setActiveTab(navInwardUnprocessed); break;
            case "inward-maker-reports": setActiveTab(navInwardMakerReports); break;

            case "inward-checker-dashboard": setActiveTab(navInwardCheckerDashboard); break;
            case "verification": setActiveTab(navVerification); break;
            case "rrf": setActiveTab(navRrf); break;
            case "inward-checker-reports": setActiveTab(navInwardCheckerReports); break;
        }
    }

    // ==================== 1. COMMON DASHBOARD ====================
    public void navToDashboard() {
        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
        if ("ADMIN".equalsIgnoreCase(role)) {
            navToAdminDashboard();
        } else if ("OUTWARD_CHECKER".equalsIgnoreCase(role)) {
            navToOutwardCheckerDashboard();
        } else if ("INWARD_MAKER".equalsIgnoreCase(role)) {
            navToInwardDashboard();
        } else if ("INWARD_CHECKER".equalsIgnoreCase(role)) {
            navToInwardCheckerDashboard();
        } else {
            navToOutwardMakerDashboard();
        }
    }

    // ==================== 2. ADMIN ROUTING ====================
    public void navToAdminDashboard() {
        navigateTo("/admin/dashboard.zul", navDashboard, "Admin Dashboard");
    }

    public void navToUserManagement() {
        navigateTo("/admin/user/user-list.zul", navAdminUsers, "User Management");
    }

    public void navToRoleManagement() {
        navigateTo("/admin/role/role-list.zul", navAdminRoles, "Role Management");
    }

    public void navToAuditLogs() {
        navigateTo("/admin/audit-log/audit-logs.zul", navAudit, "Audit Logs");
    }

    public void navToAdminReports() {
        navigateTo("/admin/reports.zul", navAdminReports, "Admin Reports");
    }

    // ==================== 3. OUTWARD MAKER ROUTING ====================
    public void navToOutwardMakerDashboard() {
        navigateTo("/outward/maker/maker-dashboard.zul", navDashboard, "Maker Dashboard");
    }

    public void navToUploadBatch() {
        navigateTo("/outward/maker/upload-batch.zul", navUpload, "Upload Batch");
    }

    public void navToOutwardMicrRepair() {
        navigateTo("/outward/maker/micr-repair.zul", navOutwardMicr, "MICR Repair");
    }

    public void navToOutwardDataEntry() {
        navigateTo("/outward/maker/data-entry.zul", navOutwardDataEntry, "Data Entry");
    }

    public void navToQueue() {
        navigateTo("/outward/maker/unprocessed-queue.zul", navQueue, "Unprocessed Queue");
    }

    public void navToOutwardMakerReports() {
        navigateTo("/outward/maker/reports.zul", navOutwardMakerReports, "Outward Reports");
    }

    // ==================== 4. OUTWARD CHECKER ROUTING ====================
    public void navToOutwardCheckerDashboard() {
        navigateTo("/outward/checker/dashboard.zul", navDashboard, "Checker Dashboard");
    }

    public void navToOutwardCheckerQueue() {
        navigateTo("/outward/checker/checker-queue.zul", navCheckerQueue, "Checker Queue");
    }

    public void navToOutwardXmlGeneration() {
        navigateTo("/outward/checker/xml-generation.zul", navXmlGen, "XML Generation");
    }

    public void navToOutwardRejectedCheques() {
        navigateTo("/outward/checker/rejected-cheques.zul", navOutwardRejected, "Rejected Cheques");
    }

    public void navToOutwardCheckerReports() {
        navigateTo("/outward/checker/reports.zul", navOutwardCheckerReports, "Checker Reports");
    }

    // ==================== 5. INWARD MAKER ROUTING ====================
    public void navToInwardDashboard() {
        navigateTo("/inward/maker/dashboard.zul", navInwardDashboard != null ? navInwardDashboard : navDashboard, "Inward Dashboard");
    }

    public void navToBatchIntake() {
        navigateTo("/inward/maker/intake/intake.zul", navBatchIntake, "Batch Intake");
    }

    public void navToInwardMicrRepair() {
        navigateTo("/inward/maker/cheque/micr_repair.zul", navInwardMicr, "MICR Repair");
    }

    public void navToInwardDataEntry() {
        navigateTo("/inward/maker/cheque/data_entry.zul", navInwardDataEntry, "Data Entry");
    }

    public void navToMakerCompletion() {
        navigateTo("/inward/maker/submission/maker_completion.zul", navMakerCompletion, "Maker Completion");
    }

    public void navToInwardUnprocessedQueue() {
        navigateTo("/inward/maker/cheque/unprocessed.zul", navInwardUnprocessed, "Unprocessed Queue");
    }

    public void navToInwardMakerReports() {
        navigateTo("/inward/maker/reports.zul", navInwardMakerReports, "Inward Reports");
    }

    // ==================== 6. INWARD CHECKER ROUTING ====================
    public void navToInwardCheckerDashboard() {
        navigateTo("/inward/checker/dashboard.zul", navInwardCheckerDashboard != null ? navInwardCheckerDashboard : navDashboard, "Checker Dashboard");
    }

    public void navToVerification() {
        navigateTo("/inward/checker/verification.zul", navVerification, "Verification");
    }

    public void navToRRF() {
        navigateTo("/inward/checker/rrf.zul", navRrf, "RRF Returns");
    }

    public void navToInwardCheckerReports() {
        navigateTo("/inward/checker/reports.zul", navInwardCheckerReports, "Checker Reports");
    }

    // ==================== 7. LOGOUT (Only Action Requiring Redirect) ====================
    public void onClickLogout() {
        String currentUser = (String) Sessions.getCurrent().getAttribute("LOGGED_USER");
        if (currentUser == null || currentUser.trim().isEmpty()) {
            currentUser = "User";
        }

        Messagebox.show("Are you sure you want to log out of Express Clear CTS?", "Confirm Logout",
            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, evt -> {
                if (Messagebox.ON_YES.equals(evt.getName())) {
                    Sessions.getCurrent().invalidate();
                    Executions.sendRedirect("/common/login.zul");
                }
            });
    }
}
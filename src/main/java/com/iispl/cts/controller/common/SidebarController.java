package com.iispl.cts.controller.common;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import java.util.Map;

public class SidebarController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Header & Category Labels
    private Label lblPortalTitle;
    private Label lblNavCategory;

    // Role Section Containers
    private Div divAdminMenu;
    private Div divMakerMenu;
    private Div divCheckerMenu;
    private Div divInwardMakerMenu;
    private Div divInwardCheckerMenu;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        applyRoleVisibility();
    }

    private void applyRoleVisibility() {
        // Priority 1: Check argument passed via <include role="ADMIN" .../>
        String role = null;
        Map<?, ?> argMap = Executions.getCurrent().getArg();
        if (argMap != null && argMap.containsKey("role")) {
            Object r = argMap.get("role");
            if (r != null) role = r.toString();
        }

        // Priority 2: Fallback to HTTP Session
        if (role == null || role.trim().isEmpty()) {
            role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
        }

        // Default to ADMIN if nothing is specified
        if (role == null || role.trim().isEmpty()) {
            role = "ADMIN";
        }

        // Hide all containers first
        if (divAdminMenu != null) divAdminMenu.setVisible(false);
        if (divMakerMenu != null) divMakerMenu.setVisible(false);
        if (divCheckerMenu != null) divCheckerMenu.setVisible(false);
        if (divInwardMakerMenu != null) divInwardMakerMenu.setVisible(false);
        if (divInwardCheckerMenu != null) divInwardCheckerMenu.setVisible(false);

        // Switch headers and show active role container
        switch (role.toUpperCase()) {
            case "ADMIN":
                if (lblPortalTitle != null) lblPortalTitle.setValue("ADMIN PORTAL");
                if (lblNavCategory != null) lblNavCategory.setValue("ADMINISTRATION");
                if (divAdminMenu != null) divAdminMenu.setVisible(true);
                break;

            case "OUTWARD_CHECKER":
                if (lblPortalTitle != null) lblPortalTitle.setValue("OUTWARD CHECKER PORTAL");
                if (lblNavCategory != null) lblNavCategory.setValue("NAVIGATION");
                if (divCheckerMenu != null) divCheckerMenu.setVisible(true);
                break;

            case "INWARD_MAKER":
                if (lblPortalTitle != null) lblPortalTitle.setValue("INWARD MAKER PORTAL");
                if (lblNavCategory != null) lblNavCategory.setValue("NAVIGATION");
                if (divInwardMakerMenu != null) divInwardMakerMenu.setVisible(true);
                break;

            case "INWARD_CHECKER":
                if (lblPortalTitle != null) lblPortalTitle.setValue("INWARD CHECKER PORTAL");
                if (lblNavCategory != null) lblNavCategory.setValue("NAVIGATION");
                if (divInwardCheckerMenu != null) divInwardCheckerMenu.setVisible(true);
                break;

            case "OUTWARD_MAKER":
            default:
                if (lblPortalTitle != null) lblPortalTitle.setValue("OUTWARD MAKER PORTAL");
                if (lblNavCategory != null) lblNavCategory.setValue("NAVIGATION");
                if (divMakerMenu != null) divMakerMenu.setVisible(true);
                break;
        }
    }

    // Admin Navigation Actions
    public void navToAdminDashboard() { Executions.sendRedirect("/admin/dashboard.zul"); }
    public void navToUserManagement() { Executions.sendRedirect("/admin/user/user-management.zul"); }
    public void navToRoleManagement() { Executions.sendRedirect("/admin/role-management.zul"); }
    public void navToAuditLogs() { Executions.sendRedirect("/admin/audit-logs.zul"); }
    public void navToAdminReports() { Executions.sendRedirect("/admin/reports.zul"); }

    // Outward Maker Navigation Actions
    public void navToMakerDashboard() { Executions.sendRedirect("/maker/dashboard.zul"); }
    public void navToUploadBatch() { Executions.sendRedirect("/maker/upload-batch.zul"); }
    public void navToOutwardMicrRepair() { Executions.sendRedirect("/maker/micr-repair.zul"); }
    public void navToOutwardDataEntry() { Executions.sendRedirect("/maker/data-entry.zul"); }
    public void navToQueue() { Executions.sendRedirect("/maker/unprocessed-cheques.zul"); }
    public void navToOutwardMakerReports() { Executions.sendRedirect("/maker/reports.zul"); }

    // Outward Checker Navigation Actions
    public void navToCheckerDashboard() { Executions.sendRedirect("/checker/dashboard.zul"); }
    public void navToOutwardCheckerQueue() { Executions.sendRedirect("/checker/queue.zul"); }
    public void navToOutwardXmlGeneration() { Executions.sendRedirect("/checker/xml-gen.zul"); }
    public void navToOutwardRejectedCheques() { Executions.sendRedirect("/checker/rejected.zul"); }
    public void navToOutwardCheckerReports() { Executions.sendRedirect("/checker/reports.zul"); }

    // Inward Maker Navigation Actions
    public void navToInwardDashboard() { Executions.sendRedirect("/inward-maker/dashboard.zul"); }
    public void navToBatchIntake() { Executions.sendRedirect("/inward-maker/intake.zul"); }
    public void navToInwardMicrRepair() { Executions.sendRedirect("/inward-maker/micr-repair.zul"); }
    public void navToInwardDataEntry() { Executions.sendRedirect("/inward-maker/data-entry.zul"); }
    public void navToMakerCompletion() { Executions.sendRedirect("/inward-maker/completion.zul"); }
    public void navToInwardUnprocessedQueue() { Executions.sendRedirect("/inward-maker/unprocessed.zul"); }
    public void navToInwardMakerReports() { Executions.sendRedirect("/inward-maker/reports.zul"); }

    // Inward Checker Navigation Actions
    public void navToInwardCheckerDashboard() { Executions.sendRedirect("/inward-checker/dashboard.zul"); }
    public void navToVerification() { Executions.sendRedirect("/inward-checker/verification.zul"); }
    public void navToRRF() { Executions.sendRedirect("/inward-checker/rrf.zul"); }
    public void navToInwardCheckerReports() { Executions.sendRedirect("/inward-checker/reports.zul"); }

    // Global Logout
    public void onClickLogout() {
        Sessions.getCurrent().invalidate();
        Executions.sendRedirect("/login.zul");
    }
}
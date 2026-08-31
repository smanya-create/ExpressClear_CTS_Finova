package com.iispl.cts.controller.common;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Include;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class SidebarController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;
    
    private Component sidebarComponent;

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
        this.sidebarComponent = comp;
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
    // Admin Navigation Actions
    public void navToAdminDashboard() { Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul"); }
    public void navToUserManagement() { Executions.sendRedirect("/admin/user/user-management.zul"); }
    public void navToRoleManagement() { Executions.sendRedirect("/admin/role/role-management.zul"); }
    public void navToAuditLogs() { Executions.sendRedirect("/admin/audit/audit-logs.zul"); }
    public void navToAdminReports() { Executions.sendRedirect("/admin/reports/reports.zul"); }

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
    public void navToInwardDashboard() { Executions.sendRedirect("/inward/maker/dashboard.zul"); }
    public void navToBatchIntake() { Executions.sendRedirect("/inward/maker/intake/batch-intake.zul"); }
       
    public void navToInwardMicrRepair() { 
    	
    	 Component root = sidebarComponent.getPage().getFirstRoot();
    	 
         Component mainContentArea = root.getFellowIfAny("mainContentArea", true);
    	
         if (mainContentArea instanceof Include) {

             Include include = (Include) mainContentArea;

             include.setSrc("/inward/maker/micr-repair/micr-repair.zul");
         }   	
    
    }
    
    public void navToInwardDataEntry() { Executions.sendRedirect("/inward/maker/data-entry/data-entry.zul"); }
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
    	AuditServiceImpl.getInstance().log("AUTH", "LOGOUT", "User logged out of the system", "SUCCESS");

        // Clear and invalidate current HTTP session
        if (Sessions.getCurrent() != null) {
            Sessions.getCurrent().invalidate();
        }

        // Redirect back to login page
        Executions.sendRedirect("/common/login.zul");
    }
}
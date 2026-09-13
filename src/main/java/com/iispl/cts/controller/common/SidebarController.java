package com.iispl.cts.controller.common;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;

import com.iispl.cts.common.util.ActiveUserManager;
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
	
	// Inward Maker Nav Links (wired automatically by ID from sidebar.zul)
	private A navInwardDashboard;
	private A navBatchIntake;
	private A navInwardMicr;
	private A navInwardDataEntry;
	private A navInwardUnprocessed;

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
			if (r != null)
				role = r.toString();
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
		if (divAdminMenu != null)
			divAdminMenu.setVisible(false);
		if (divMakerMenu != null)
			divMakerMenu.setVisible(false);
		if (divCheckerMenu != null)
			divCheckerMenu.setVisible(false);
		if (divInwardMakerMenu != null)
			divInwardMakerMenu.setVisible(false);
		if (divInwardCheckerMenu != null)
			divInwardCheckerMenu.setVisible(false);

		// Switch headers and show active role container
		switch (role.toUpperCase()) {
		case "ADMIN":
			if (lblPortalTitle != null)
				lblPortalTitle.setValue("ADMIN PORTAL");
			if (lblNavCategory != null)
				lblNavCategory.setValue("ADMINISTRATION");
			if (divAdminMenu != null)
				divAdminMenu.setVisible(true);
			break;

		case "OUTWARD_CHECKER":
			if (lblPortalTitle != null)
				lblPortalTitle.setValue("OUTWARD CHECKER PORTAL");
			if (lblNavCategory != null)
				lblNavCategory.setValue("NAVIGATION");
			if (divCheckerMenu != null)
				divCheckerMenu.setVisible(true);
			break;

		case "INWARD_MAKER":
			if (lblPortalTitle != null)
				lblPortalTitle.setValue("INWARD MAKER PORTAL");
			if (lblNavCategory != null)
				lblNavCategory.setValue("NAVIGATION");
			if (divInwardMakerMenu != null)
				divInwardMakerMenu.setVisible(true);
			break;

		case "INWARD_CHECKER":
			if (lblPortalTitle != null)
				lblPortalTitle.setValue("INWARD CHECKER PORTAL");
			if (lblNavCategory != null)
				lblNavCategory.setValue("NAVIGATION");
			if (divInwardCheckerMenu != null)
				divInwardCheckerMenu.setVisible(true);
			break;

		case "OUTWARD_MAKER":
		default:
			if (lblPortalTitle != null)
				lblPortalTitle.setValue("OUTWARD MAKER PORTAL");
			if (lblNavCategory != null)
				lblNavCategory.setValue("NAVIGATION");
			if (divMakerMenu != null)
				divMakerMenu.setVisible(true);
			break;
		}
	}

	// Admin Navigation Actions
	public void navToAdminDashboard() {
		Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
	}

	public void navToUserManagement() {
		Executions.sendRedirect("/admin/user/user-management.zul");
	}

	public void navToRoleManagement() {
		Executions.sendRedirect("/admin/role/role-management.zul");
	}

	public void navToAuditLogs() {
		Executions.sendRedirect("/admin/audit/audit-logs.zul");
	}

	public void navToAdminReports() {
		Executions.sendRedirect("/admin/reports/admin_reports.zul");
	}

	// Outward Maker Navigation Actions
	public void navToMakerDashboard() {
		Component root = sidebarComponent.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {
			Include include = (Include) mainContentArea;
			include.setSrc("/outward/maker/dashboard.zul");
		}
	}

	public void navToUploadBatch() {
		Component root = sidebarComponent.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {

			Include include = (Include) mainContentArea;

			include.setSrc("/outward/maker/batch/batch-upload.zul");
		}

	}

	public void navToOutwardMicrRepair() {
		Component root = sidebarComponent.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {

			Include include = (Include) mainContentArea;

			include.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");
		}

	}

	public void navToQueue() {
		Component root = sidebarComponent.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {

			Include include = (Include) mainContentArea;

			include.setSrc("/outward/maker/unprocessed-cheques.zul");
		}

	}

	public void navToOutwardDataEntry() {
		Component root = sidebarComponent.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {
			Include include = (Include) mainContentArea;
			include.setSrc("/outward/maker/data-entry.zul");
		}
	}

	public void navToOutwardMakerReports() {
	    Component root = sidebarComponent.getPage().getFirstRoot();

	    Component mainContentArea =
	            root.getFellowIfAny("mainContentArea", true);

	    if (mainContentArea instanceof Include) {
	        Include include = (Include) mainContentArea;

	        include.setSrc(
	            "/outward/maker/reports/maker-reports.zul"
	        );
	    }
	}

	// Outward Checker Navigation Actions
	public void navToCheckerDashboard() {
		Executions.sendRedirect("/outward/checker/dashboard.zul");
	}

	public void navToOutwardCheckerQueue() {
		Executions.sendRedirect("/outward/checker/checker-queue.zul");
	}

	public void navToOutwardXmlGeneration() {
		Executions.sendRedirect("/outward/checker/xml-generation.zul");
	}

	public void navToOutwardRejectedCheques() {
		Executions.sendRedirect("/outward/checker/rejected-cheques.zul");
	}

	public void navToOutwardCheckerReports() {
		Executions.sendRedirect("/outward/checker/cheque-reports.zul");
	}

	public void navToOutwardCheckerUnprocessedQueue() {
		Executions.sendRedirect("/outward/checker/checker-unprocessed-cheques.zul");
	}

	// Inward Maker Navigation Actions
	public void navToInwardDashboard() {
		navigateTo("/inward/maker/dashboard.zul", "Maker Dashboard", navInwardDashboard);
	}

	public void navToBatchIntake() {
	    navigateTo("/inward/maker/batch/batch-list.zul", "Batch Intake", navBatchIntake);
	}

	public void navToInwardMicrRepair() {
		navigateTo("/inward/maker/micr-repair/micr-repair-queue.zul", "MICR Repair", navInwardMicr);
	}

	public void navToInwardDataEntry() {
		navigateTo("/inward/maker/data-entry/data-entry-batches.zul", "Data Entry", navInwardDataEntry);
	}

	public void navToInwardUnprocessedQueue() {
		navigateTo("/inward-maker/unprocessed.zul", "Unprocessed Cheques", navInwardUnprocessed);
	}
		
	// Inward Checker Navigation Actions
	public void navToInwardCheckerDashboard() {
		Executions.sendRedirect("/inward/checker/dashboard.zul");
	}

	public void navToVerification() {
		Executions.sendRedirect("/inward/checker/verification.zul");
	}

	public void navToRRF() {
		Executions.sendRedirect("/inward/checker/return.zul");
	}

	public void navToInwardCheckerReports() {
		Executions.sendRedirect("/inward/checker/reports.zul");
	}

	// Global Logout
	public void onClickLogout() {
		AuditServiceImpl.getInstance().log("AUTH", "LOGOUT", "User logged out of the system", "SUCCESS");

		// Clear and invalidate current HTTP session
		if (Sessions.getCurrent() != null) {
			String userId = (String) Sessions.getCurrent().getAttribute("USER_ID");
			if (userId == null) {
				userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
			}

			// Deregister user from real-time tracker
			ActiveUserManager.userLoggedOut(userId);

			Sessions.getCurrent().invalidate();
		}

		// Redirect back to login page
		Executions.sendRedirect("/common/login.zul");
	}
	
	public void navigateTo(String zulPath, String pageSubtitle, A activeNavLink) {
		Component root = sidebarComponent.getPage().getFirstRoot();
		if (root == null) return;

		// 1. Swap the SPA content area
		Component mainContent = root.getFellowIfAny("mainContentArea", true);
		if (mainContent instanceof Include) {
			Include include = (Include) mainContent;
			include.setSrc(null);
			include.setSrc(zulPath);
		}

		// 2. Find and update Header Subtitle (checks ID spaces across desktop pages)
		Label lblSubtitle = findSubtitleLabel(root);
		if (lblSubtitle != null) {
			lblSubtitle.setValue(pageSubtitle);
		}

		// 3. Update sidebar active tab highlight
		clearInwardActiveTabs();
		if (activeNavLink != null) {
			activeNavLink.setSclass("nav-item active");
		}
	}

	// Helper to reliably locate lblPageSubtitle across nested includes/pages
	private Label findSubtitleLabel(Component root) {
		// Attempt 1: Direct fellow lookup
		Component comp = root.getFellowIfAny("lblPageSubtitle", true);
		if (comp instanceof Label) return (Label) comp;

		// Attempt 2: Search across all desktop pages (handles isolated include spaces)
		if (sidebarComponent.getDesktop() != null) {
			for (org.zkoss.zk.ui.Page page : sidebarComponent.getDesktop().getPages()) {
				comp = page.getFellowIfAny("lblPageSubtitle", true);
				if (comp instanceof Label) return (Label) comp;
			}
		}

		// Attempt 3: Recursive depth search down the component tree
		return findLabelRecursively(root, "lblPageSubtitle");
	}

	private Label findLabelRecursively(Component parent, String id) {
		if (id.equals(parent.getId()) && parent instanceof Label) {
			return (Label) parent;
		}
		for (Component child : parent.getChildren()) {
			Label found = findLabelRecursively(child, id);
			if (found != null) return found;
		}
		return null;
	}
	
	private void clearInwardActiveTabs() {
		if (navInwardDashboard != null) navInwardDashboard.setSclass("nav-item");
		if (navBatchIntake != null) navBatchIntake.setSclass("nav-item");
		if (navInwardMicr != null) navInwardMicr.setSclass("nav-item");
		if (navInwardDataEntry != null) navInwardDataEntry.setSclass("nav-item");
		if (navInwardUnprocessed != null) navInwardUnprocessed.setSclass("nav-item");
	}

}
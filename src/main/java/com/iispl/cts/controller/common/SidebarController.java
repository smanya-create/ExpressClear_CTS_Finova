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

	// Header and category labels
	private Label lblPortalTitle;
	private Label lblNavCategory;

	// Menu containers by role
	private Div divAdminMenu;
	private Div divMakerMenu;
	private Div divCheckerMenu;
	private Div divInwardMakerMenu;
	private Div divInwardCheckerMenu;
	
	// Inward maker nav links
	private A navInwardDashboard;
	private A navBatchIntake;
	private A navInwardMicr;
	private A navInwardDataEntry;
	private A navInwardUnprocessed;
	
	// Inward checker nav links
	private A navInwardCheckerDash;
	private A navVerification;
	private A navInwardCheckerReports;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		this.sidebarComponent = comp;
		applyRoleVisibility();
	}

	private void applyRoleVisibility() {
		// Check include parameter first, then session attribute
		String role = null;
		Map<?, ?> argMap = Executions.getCurrent().getArg();
		if (argMap != null && argMap.containsKey("role")) {
			Object r = argMap.get("role");
			if (r != null)
				role = r.toString();
		}

		if (role == null || role.trim().isEmpty()) {
			role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
		}

		if (role == null || role.trim().isEmpty()) {
			role = "ADMIN";
		}

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

			// Highlight active inward tab based on URL param
			String pageParam = Executions.getCurrent().getParameter("page");
			if ("data-entry".equals(pageParam) || "data-entry-queue".equals(pageParam)) {
				clearInwardActiveTabs();
				if (navInwardDataEntry != null) navInwardDataEntry.setSclass("nav-item active");
			} else if ("micr-repair".equals(pageParam) || "micr-repair-queue".equals(pageParam)) {
				clearInwardActiveTabs();
				if (navInwardMicr != null) navInwardMicr.setSclass("nav-item active");
			} else if ("batch-intake".equals(pageParam)) {
				clearInwardActiveTabs();
				if (navBatchIntake != null) navBatchIntake.setSclass("nav-item active");
			} else {
				clearInwardActiveTabs();
				if (navInwardDashboard != null) navInwardDashboard.setSclass("nav-item active");
			}
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

	// Admin actions
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

	// Outward maker actions
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
		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {
			Include include = (Include) mainContentArea;
			include.setSrc("/outward/maker/reports/maker-reports.zul");
		}
	}

	// Outward checker actions
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

	// Inward maker actions
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

	// Inward checker actions
	public void navToInwardCheckerDashboard() {
		Executions.sendRedirect("/inward/checker/dashboard.zul");
	}

	public void navToVerification() {
		Executions.sendRedirect("/inward/checker/verification.zul");
	}

	public void navToInwardCheckerReports() {
		Executions.sendRedirect("/inward/checker/reports.zul");
	}
	
	public void onClickLogout() {
		AuditServiceImpl.getInstance().log("AUTH", "LOGOUT", "User logged out of the system", "SUCCESS");

		if (Sessions.getCurrent() != null) {
			String userId = (String) Sessions.getCurrent().getAttribute("USER_ID");
			if (userId == null) {
				userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
			}

			ActiveUserManager.userLoggedOut(userId);
			Sessions.getCurrent().invalidate();
		}

		Executions.sendRedirect("/common/login.zul");
	}
	
	public void navigateTo(String zulPath, String pageSubtitle, A activeNavLink) {
		Component root = sidebarComponent.getPage().getFirstRoot();
		if (root == null) return;

		// Swap view inside main include
		Component mainContent = root.getFellowIfAny("mainContentArea", true);
		if (mainContent instanceof Include) {
			Include include = (Include) mainContent;
			include.setSrc(null);
			include.setSrc(zulPath);
		}

		Label lblSubtitle = findSubtitleLabel(root);
		if (lblSubtitle != null) {
			lblSubtitle.setValue(pageSubtitle);
		}

		clearInwardActiveTabs();
		if (activeNavLink != null) {
			activeNavLink.setSclass("nav-item active");
		}
	}

	private Label findSubtitleLabel(Component root) {
		Component comp = root.getFellowIfAny("lblPageSubtitle", true);
		if (comp instanceof Label) return (Label) comp;

		// Search across desktop pages for nested include scopes
		if (sidebarComponent.getDesktop() != null) {
			for (org.zkoss.zk.ui.Page page : sidebarComponent.getDesktop().getPages()) {
				comp = page.getFellowIfAny("lblPageSubtitle", true);
				if (comp instanceof Label) return (Label) comp;
			}
		}

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
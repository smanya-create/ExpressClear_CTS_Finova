package com.iispl.cts.common.util;

import java.time.format.DateTimeFormatter;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.impl.InputElement;

public class SecurityUtil {

	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

	public static boolean hasPermission(String screenKey) {
		Session session = Sessions.getCurrent();
		if (session == null)
			return false;

		// Admin role retains full access across all application screens
		String role = (String) session.getAttribute("USER_ROLE");
		if (role != null && role.toUpperCase().contains("ADMIN")) {
			return true;
		}

		Object permsObj = session.getAttribute("USER_PERMISSIONS");
		if (permsObj == null || permsObj.toString().trim().isEmpty()) {
			return false;
		}

		String[] permissions = permsObj.toString().split(",");
		String targetKey = screenKey.trim().toUpperCase();

		for (String perm : permissions) {
			String p = perm.trim().toUpperCase();
			if (p.equals(targetKey) || p.equals("ALL") || p.equals("*")) {
				return true;
			}
		}
		return false;
	}

	// Entry-point screen guard validating session existence and
	// authorization.Redirects unauthenticated or unauthorized requests to
	// respective error views.

	public static boolean checkAccess(String screenKey) {
		Session session = Sessions.getCurrent();
		if (session == null) {
			Executions.sendRedirect("/common/login.zul");
			return false;
		}

		// Verify active user identifier across potential session attribute keys
		Object user = session.getAttribute("LOGGED_USER");
		if (user == null)
			user = session.getAttribute("CTS_USER_ID");
		if (user == null)
			user = session.getAttribute("USER_ID");

		if (user == null) {
			Executions.sendRedirect("/common/login.zul");
			return false;
		}

		if (!hasPermission(screenKey)) {
			Executions.sendRedirect("/common/access-denied.zul");
			return false;
		}

		return true;
	}

	// Recursively traverses the view tree to disable mutation controls (buttons,
	// inputs)when the clearing session is closed or outside scheduled presentation
	// windows.

	public static void applySessionLockdown(Page page) {
		Session session = Sessions.getCurrent();
		if (session == null || page == null)
			return;

		// 1. Admin users bypass window and session lockdowns
		String role = (String) session.getAttribute("USER_ROLE");
		if (role == null)
			role = (String) session.getAttribute("CTS_USER_ROLE");
		if (role != null && role.toUpperCase().contains("ADMIN")) {
			return;
		}

		Boolean isOpen = (Boolean) session.getAttribute("CTS_SESSION_OPEN");
		String path = (page.getRequestPath() != null) ? page.getRequestPath().toLowerCase() : "";

		// Condition A: If DB session is CLOSED, lock everything
		boolean lockAll = !Boolean.TRUE.equals(isOpen);

		// Condition B: If Outward screen and not morning window, lock
		boolean lockOutward = path.contains("/outward/") && !ClearingTimeMock.isOutwardWindow();

		// Condition C: If Inward screen and not afternoon window, lock
		boolean lockInward = path.contains("/inward/") && !ClearingTimeMock.isInwardWindow();

		if (lockAll || lockOutward || lockInward) {
			// Dynamically resolve cutoff time string to keep warnings and tooltips in sync
			String cutoffStr = ClearingTimeMock.CUTOFF_TIME.format(TIME_FMT);

			String reason = lockAll ? "Session is CLOSED by Admin."
					: (lockOutward ? "Outward Cutoff reached (" + cutoffStr + "). Presentation window closed."
							: "Inward opens after Outward cutoff at " + cutoffStr + ".");

			for (Component root : page.getRoots()) {
				disableActionControls(root, reason);
			}
		}
	}

	// Helper to disable mutable action buttons and set inputs to readonly,while
	// preserving core navigation, view, and search controls.

	private static void disableActionControls(Component comp, String tooltip) {
		if (comp instanceof Button) {
			Button btn = (Button) comp;
			String id = (btn.getId() != null) ? btn.getId().toLowerCase() : "";
			String label = (btn.getLabel() != null) ? btn.getLabel().trim().toLowerCase() : "";

			// System navigation controls are kept active
			boolean isSystemControl = id.contains("logout") || id.contains("search") || id.contains("view")
					|| id.contains("refresh") || id.contains("page") || label.equals("«") || label.equals("‹")
					|| label.equals("›") || label.equals("»");

			if (!isSystemControl) {
				btn.setDisabled(true);
				btn.setTooltiptext(tooltip);
				btn.setSclass("btn-grid-action btn-grid-action-disabled");
			}
		} else if (comp instanceof InputElement) {
			((InputElement) comp).setReadonly(true);
		}

		// Traverse nested children components
		for (Component child : comp.getChildren()) {
			disableActionControls(child, tooltip);
		}
	}
}
package com.iispl.cts.listener;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.EventInterceptor;
import org.zkoss.zul.Button;
import com.iispl.cts.common.util.ClearingTimeMock;

public class GlobalSessionLockInterceptor implements EventInterceptor {

	// Intercepts UI button clicks to enforce session open/closed state and cycle
	// cutoffs
	@Override
	public Event beforeProcessEvent(Event event) {
		if ("onClick".equals(event.getName())) {
			Component target = event.getTarget();

			if (target instanceof Button) {
				// Admin bypasses session locks
				if (isAdminUser()) {
					return event;
				}

				Button btn = (Button) target;
				String label = btn.getLabel() != null ? btn.getLabel().trim().toLowerCase() : "";
				String id = btn.getId() != null ? btn.getId().toLowerCase() : "";

				// Allow non-mutating actions like navigation, filters, modal close, and exports
				if (isSafeReadOnlyAction(label, id)) {
					return event;
				}

				// Block mutating actions if clearing session is closed
				Object sessionOpen = (Sessions.getCurrent() != null)
						? Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN")
						: null;

				if (Boolean.FALSE.equals(sessionOpen)) {
					event.stopPropagation();
					Clients.showNotification("Clearing session is CLOSED. Mutating actions are locked.", "warning",
							null, "top_center", 2500);
					return null;
				}

				// Check outward/inward operating time windows based on page path
				String requestPath = "";
				if (target.getPage() != null && target.getPage().getRequestPath() != null) {
					requestPath = target.getPage().getRequestPath().toLowerCase();
				}

				boolean isOutwardPage = requestPath.contains("/outward/");
				boolean isInwardPage = requestPath.contains("/inward/");

				if (isOutwardPage && !ClearingTimeMock.isOutwardWindow()) {
					event.stopPropagation();
					Clients.showNotification("Outward Cutoff reached (02:30 PM). Presentation window is closed.",
							"warning", null, "top_center", 3000);
					return null;
				}

				if (isInwardPage && !ClearingTimeMock.isInwardWindow()) {
					event.stopPropagation();
					Clients.showNotification("Inward processing opens only after Outward cutoff at 02:30 PM.",
							"warning", null, "top_center", 3000);
					return null;
				}
			}
		}
		return event;
	}

	// Identifies buttons that do not modify state and can run during locked
	// sessions
	private boolean isSafeReadOnlyAction(String label, String id) {
		// Pagination controls
		if (label.equals("«") || label.equals("‹") || label.equals("›") || label.equals("»") || label.equals("<")
				|| label.equals(">") || label.equals("<<") || label.equals(">>")) {
			return true;
		}

		// Read-only and navigation labels
		if (label.contains("search") || label.contains("filter") || label.contains("find") || label.contains("reset")
				|| label.contains("clear") || label.contains("refresh") || label.contains("view")
				|| label.contains("details") || label.contains("inspect") || label.contains("preview")
				|| label.contains("image") || label.contains("front") || label.contains("back")
				|| label.contains("export") || label.contains("download") || label.contains("print")
				|| label.contains("report") || label.contains("close") || label.contains("cancel")
				|| label.contains("back") || label.contains("dismiss") || label.contains("logout")
				|| label.contains("log out")) {
			return true;
		}

		// Component ID matches for view, search, and export elements
		return id.contains("search") || id.contains("filter") || id.contains("reset") || id.contains("refresh")
				|| id.contains("view") || id.contains("detail") || id.contains("preview") || id.contains("img")
				|| id.contains("export") || id.contains("download") || id.contains("print") || id.contains("report")
				|| id.contains("close") || id.contains("cancel") || id.contains("back") || id.contains("page")
				|| id.contains("logout");
	}

	// Checks session attributes to verify if active user has administrator
	// privileges
	private boolean isAdminUser() {
		if (Sessions.getCurrent() == null) {
			return false;
		}

		Object role = Sessions.getCurrent().getAttribute("USER_ROLE");
		if (role == null)
			role = Sessions.getCurrent().getAttribute("ROLE");
		if (role == null)
			role = Sessions.getCurrent().getAttribute("USER_TYPE");
		if (role == null)
			role = Sessions.getCurrent().getAttribute("ROLE_NAME");
		if (role == null)
			role = Sessions.getCurrent().getAttribute("CTS_USER_ROLE");

		if (role != null && role.toString().toUpperCase().contains("ADMIN")) {
			return true;
		}

		Object user = Sessions.getCurrent().getAttribute("LOGGED_IN_USER");
		if (user == null)
			user = Sessions.getCurrent().getAttribute("USER");
		if (user == null)
			user = Sessions.getCurrent().getAttribute("USERNAME");

		return user != null && user.toString().toUpperCase().contains("ADMIN");
	}

	// Post-execution callback for event processing lifecycle
	@Override
	public void afterProcessEvent(Event event) {
	}

	// Pre-send callback before event is transmitted to the client
	@Override
	public Event beforeSendEvent(Event event) {
		return event;
	}

	// Pre-post callback before event is queued for processing
	@Override
	public Event beforePostEvent(Event event) {
		return event;
	}
}
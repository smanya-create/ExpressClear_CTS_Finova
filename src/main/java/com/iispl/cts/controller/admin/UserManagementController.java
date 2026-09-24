package com.iispl.cts.controller.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.entity.Role;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;
import com.iispl.cts.serviceimpl.RoleServiceImpl;
import com.iispl.cts.serviceimpl.UserServiceImpl;

public class UserManagementController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	// View Containers & Shared Header
	private Vlayout viewUserList;
	private Vlayout viewAddUser;
	private Vlayout viewModifyUser;
	private Include incHeader;

	// View 1 (List) Controls
	private Textbox txtSearchQuery;
	private Combobox cmbRoleFilter;
	private Combobox cmbStatusFilter;
	private Label lblUserCount;
	private Rows rowsUsers;
	private Button btnSearch;
	private Button btnClearFilter;
	private Button btnAddUser;

	// Custom Pagination Toolbar Controls («, ‹, [ 1 ] / N, ›, »)
	private Button btnFirstPage;
	private Button btnPrevPage;
	private Intbox ibCurrentPage;
	private Label lblTotalPages;
	private Button btnNextPage;
	private Button btnLastPage;

	// Pagination State
	private static final int PAGE_SIZE = 10;
	private int activePageIndex = 0;
	private int totalPages = 1;
	private List<User> currentFilteredUsers = new ArrayList<>();

	// View 2 (Add) Controls
	private Textbox txtAddEmployeeId;
	private Textbox txtAddUsername;
	private Textbox txtAddEmail;
	private Textbox txtAddPhone;
	private Textbox txtAddPassword;
	private A btnTogglePassword;
	private Combobox cmbAddRole;
	private Button btnSubmitAddUser;
	private Button btnClearAddForm;
	private Button btnCancelAdd;
	private boolean isPasswordVisible = false;

	// View 3 (Modify) Controls
	private Textbox txtModEmployeeId;
	private Textbox txtModUsername;
	private Textbox txtModEmail;
	private Textbox txtModCurrentRole;
	private Button btnActionEnable;
	private Button btnActionDisable;
	private Button btnActionChangeRole;
	private Vlayout divNewRoleContainer;
	private Combobox cmbNewRole;
	private Button btnSaveModifications;
	private Button btnCancelModifications;
	private String selectedModifyAction = "CHANGE_ROLE";
	private User currentModUser;
	private Div dotUserStatus;

	private Label lblModStatusText;
	private Div badgeUserStatus;
	private Button btnToggleUserStatus;
	private Checkbox chkChangeRole;


	// Tracks current in-memory status changes before hitting DB
	private boolean modUserActiveState;

	private final UserService userService = UserServiceImpl.getInstance();
	private final RoleService roleService = RoleServiceImpl.getInstance();

	// Cache of roleId -> roleName dynamically loaded from PostgreSQL
	private final Map<String, String> roleMap = new LinkedHashMap<>();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		if (!SecurityUtil.checkAccess(null)) {
			return;
		}
		super.doAfterCompose(comp);

		refreshRoleCache();
		populateRoleFilterDropdown();

		if (cmbRoleFilter != null && cmbRoleFilter.getItemCount() > 0) cmbRoleFilter.setSelectedIndex(0);
		if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) cmbStatusFilter.setSelectedIndex(0);

		loadUserData();
		switchView("LIST");
	}

	private void refreshRoleCache() {
		roleMap.clear();
		List<Role> roles = roleService.getAllRoles();
		if (roles != null) {
			for (Role r : roles) {
				if ("Active".equalsIgnoreCase(r.getStatus())) {
					roleMap.put(r.getRoleId(), r.getRoleName());
				}
			}
		}
	}

	private void populateRoleFilterDropdown() {
		if (cmbRoleFilter == null) return;

		cmbRoleFilter.getItems().clear();
		Comboitem allItem = new Comboitem("All Roles");
		allItem.setValue("ALL");
		cmbRoleFilter.appendChild(allItem);

		for (Map.Entry<String, String> entry : roleMap.entrySet()) {
			Comboitem item = new Comboitem(entry.getValue());
			item.setValue(entry.getKey());
			cmbRoleFilter.appendChild(item);
		}
	}

	private void populateAddRoleDropdown() {
		if (cmbAddRole == null) return;
		cmbAddRole.getItems().clear();

		for (Map.Entry<String, String> entry : roleMap.entrySet()) {
			Comboitem item = new Comboitem(entry.getValue());
			item.setValue(entry.getKey());
			cmbAddRole.appendChild(item);
		}
	}

	private void switchView(String target) {
		viewUserList.setVisible("LIST".equals(target));
		viewAddUser.setVisible("ADD".equals(target));
		viewModifyUser.setVisible("MODIFY".equals(target));

		if (incHeader != null) {
			if ("ADD".equals(target)) {
				incHeader.setDynamicProperty("pageSubtitle", "User Management / Add User");
			} else if ("MODIFY".equals(target)) {
				incHeader.setDynamicProperty("pageSubtitle", "User Management / Modify User");
			} else {
				incHeader.setDynamicProperty("pageSubtitle", "User Management");
			}
			incHeader.invalidate();
		}
	}

	private String getRoleDisplayName(String roleId) {
		return roleMap.getOrDefault(roleId, roleId);
	}

	// --- SCREEN 1: LIST VIEW & PAGINATION LOGIC ---

	private void loadUserData() {
		String query = txtSearchQuery != null ? txtSearchQuery.getValue() : "";
		String roleId = (cmbRoleFilter != null && cmbRoleFilter.getSelectedItem() != null)
				? (String) cmbRoleFilter.getSelectedItem().getValue() : "ALL";
		String status = (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null)
				? (String) cmbStatusFilter.getSelectedItem().getValue() : "ALL";

		List<User> result = userService.searchUsers(query, roleId, status);
		this.currentFilteredUsers = (result != null) ? result : new ArrayList<>();

		int totalRecords = this.currentFilteredUsers.size();
		this.totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
		if (this.totalPages < 1) {
			this.totalPages = 1;
		}

		renderPage(0);
	}

	private void renderPage(int pageIndex) {
		int totalRecords = this.currentFilteredUsers.size();

		if (pageIndex >= totalPages) {
			pageIndex = totalPages - 1;
		}
		if (pageIndex < 0) {
			pageIndex = 0;
		}
		this.activePageIndex = pageIndex;

		if (lblUserCount != null) {
			lblUserCount.setValue(totalRecords + " users found");
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

		int from = activePageIndex * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, totalRecords);
		List<User> pageSlice = (totalRecords > 0 && from < totalRecords) 
				? this.currentFilteredUsers.subList(from, to) 
						: Collections.emptyList();

		renderUserRows(pageSlice);
	}

	private void renderUserRows(List<User> users) {
		if (rowsUsers == null) return;
		rowsUsers.getChildren().clear();

		for (final User user : users) {
			Row row = new Row();

			Label lblId = new Label(user.getEmployeeId());
			lblId.setStyle("color: #4a5568; font-weight: 500; font-size: 13px;");
			row.appendChild(lblId);

			Label lblName = new Label(user.getUsername());
			lblName.setStyle("color: #2d3748; font-weight: 500; font-size: 13px;");
			row.appendChild(lblName);

			Label lblEmail = new Label(user.getEmail());
			lblEmail.setStyle("color: #4a5568; font-size: 13px;");
			row.appendChild(lblEmail);

			Label lblPhone = new Label(user.getMobileNumber() != null ? user.getMobileNumber() : "-");
			lblPhone.setStyle("color: #4a5568; font-size: 13px;");
			row.appendChild(lblPhone);

			Label lblRole = new Label(getRoleDisplayName(user.getRoleId()));
			lblRole.setStyle("color: #4a5568; font-size: 13px; text-align: left !important; display: block; width: 100%; padding-left: 12px;");
			lblRole.setSclass("cell-text");
			row.appendChild(lblRole);

			boolean isActive = "ACTIVE".equalsIgnoreCase(user.getStatus());

			// Centering wrapper container
			Div statusContainer = new Div();
			statusContainer.setStyle("display: flex; justify-content: center; align-items: center; width: 100%; text-align: center;");

			Label lblStatus = new Label(isActive ? "Active" : "Inactive");
			lblStatus.setSclass(isActive ? "badge-status badge-active" : "badge-status badge-inactive");
			lblStatus.setStyle("margin: 0 auto;");

			statusContainer.appendChild(lblStatus);
			row.appendChild(statusContainer);

			Button btnModify = new Button("Modify");
			btnModify.setIconSclass("z-icon-pencil");
			btnModify.setSclass("cts-btn-modify-row");
			btnModify.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) {
					openModifyView(user);
				}
			});
			row.appendChild(btnModify);

			rowsUsers.appendChild(row);
		}
	}

	// --- SEARCH & FILTER ACTIONS ---

	public void onClick$btnSearch(Event event) { 
		loadUserData(); 
	}

	public void onOK$txtSearchQuery(Event event) {
		loadUserData();
	}

	public void onClick$btnClearFilter(Event event) {
		if (txtSearchQuery != null) txtSearchQuery.setValue("");
		if (cmbRoleFilter != null) cmbRoleFilter.setSelectedIndex(0);
		if (cmbStatusFilter != null) cmbStatusFilter.setSelectedIndex(0);
		loadUserData();
	}

	// --- PAGINATION TOOLBAR ACTIONS ---

	public void onClick$btnFirstPage(Event event) {
		if (activePageIndex > 0) {
			renderPage(0);
		}
	}

	public void onClick$btnPrevPage(Event event) {
		if (activePageIndex > 0) {
			renderPage(activePageIndex - 1);
		}
	}

	public void onClick$btnNextPage(Event event) {
		if (activePageIndex < totalPages - 1) {
			renderPage(activePageIndex + 1);
		}
	}

	public void onClick$btnLastPage(Event event) {
		if (activePageIndex < totalPages - 1) {
			renderPage(totalPages - 1);
		}
	}

	public void onChange$ibCurrentPage(Event event) {
		Integer target = ibCurrentPage.getValue();
		if (target == null || target < 1) {
			target = 1;
		} else if (target > totalPages) {
			target = totalPages;
		}
		renderPage(target - 1);
	}

	public void onOK$ibCurrentPage(Event event) {
		onChange$ibCurrentPage(event);
	}

	// --- SCREEN 2: ADD USER VIEW ---

	private static final java.util.regex.Pattern NAME_PATTERN = 
			java.util.regex.Pattern.compile("^[a-zA-Z][a-zA-Z0-9._\\s]{2,49}$");
	private static final java.util.regex.Pattern EMAIL_PATTERN = 
			java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
	private static final java.util.regex.Pattern PHONE_PATTERN = 
			java.util.regex.Pattern.compile("^[6-9]\\d{9}$"); // Standard 10-digit mobile starting 6-9
	// Password must contain at least: 8 chars, 1 uppercase, 1 lowercase, 1 number, and 1 special character
	private static final java.util.regex.Pattern STRONG_PASSWORD_PATTERN = 
			java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$");

	public void onClick$btnAddUser(Event event) {
		txtAddEmployeeId.setValue(userService.generateNextEmployeeId());
		txtAddUsername.setValue("");
		txtAddEmail.setValue("");
		txtAddPhone.setValue("");
		txtAddPassword.setValue("");
		isPasswordVisible = false;
		txtAddPassword.setType("password");
		updatePasswordToggleIcon(false);

		refreshRoleCache();
		populateAddRoleDropdown();

		// Fix initial glitch: clear selection, reset text, and close popup
		cmbAddRole.setSelectedIndex(-1);
		cmbAddRole.setValue("");
		cmbAddRole.close();

		switchView("ADD");
	}

	public void onClick$btnTogglePassword(Event event) {
		isPasswordVisible = !isPasswordVisible;
		txtAddPassword.setType(isPasswordVisible ? "text" : "password");
		updatePasswordToggleIcon(isPasswordVisible);
	}
	private void updatePasswordToggleIcon(boolean isVisible) {
		if (btnTogglePassword != null) {
			btnTogglePassword.getChildren().clear();
			org.zkoss.zul.Span icon = new org.zkoss.zul.Span();
			icon.setSclass(isVisible ? "z-icon-eye-slash" : "z-icon-eye");
			btnTogglePassword.appendChild(icon);
		}
	}

	public void onClick$btnSubmitAddUser(Event event) {
		String empId = txtAddEmployeeId.getValue();
		String username = txtAddUsername.getValue() != null ? txtAddUsername.getValue().trim() : "";
		String email = txtAddEmail.getValue() != null ? txtAddEmail.getValue().trim() : "";
		String phone = txtAddPhone.getValue() != null ? txtAddPhone.getValue().trim() : "";
		String password = txtAddPassword.getValue() != null ? txtAddPassword.getValue().trim() : "";
		Comboitem selectedRole = cmbAddRole.getSelectedItem();

		// 1. Username / Name Validation
		if (username.isEmpty()) {
			Clients.showNotification("User Name is required.", "error", txtAddUsername, "top_center", 2500);
			txtAddUsername.focus();
			return;
		}
		if (!NAME_PATTERN.matcher(username).matches()) {
			Clients.showNotification("User Name must be 3-50 characters (letters, numbers, underscores, dots).", "error", txtAddUsername, "top_center", 3000);
			txtAddUsername.focus();
			return;
		}

		// 2. Email Validation
		if (email.isEmpty()) {
			Clients.showNotification("Email is required.", "error", txtAddEmail, "top_center", 2500);
			txtAddEmail.focus();
			return;
		}
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			Clients.showNotification("Enter a valid email address (e.g. user@domain.com).", "error", txtAddEmail, "top_center", 3000);
			txtAddEmail.focus();
			return;
		}

		// 3. Phone Number Validation
		if (phone.isEmpty()) {
			Clients.showNotification("Phone number is required.", "error", txtAddPhone, "top_center", 2500);
			txtAddPhone.focus();
			return;
		}
		if (!PHONE_PATTERN.matcher(phone).matches()) {
			Clients.showNotification("Enter a valid 10-digit mobile number starting with 6, 7, 8, or 9.", "error", txtAddPhone, "top_center", 3000);
			txtAddPhone.focus();
			return;
		}

		// 4. Password Combination Validation
		if (password.isEmpty()) {
			Clients.showNotification("Password is required.", "error", txtAddPassword, "top_center", 2500);
			txtAddPassword.focus();
			return;
		}
		if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
			Clients.showNotification(
					"Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character (e.g., User@123#).", 
					"error", txtAddPassword, "top_center", 4000);
			txtAddPassword.focus();
			return;
		}

		// 5. Role Selection Validation
		if (selectedRole == null || selectedRole.getValue() == null) {
			Clients.showNotification("Please select a role.", "error", cmbAddRole, "top_center", 2500);
			cmbAddRole.focus();
			return;
		}

		// Duplicate username pre-check
		if (userService.findByUsername(username) != null) {
			Clients.showNotification("Username '" + username + "' already exists.", "error", txtAddUsername, "top_center", 3000);
			txtAddUsername.focus();
			return;
		}

		String assignedRoleId = (String) selectedRole.getValue();
		String roleDisplayName = getRoleDisplayName(assignedRoleId);

		User newUser = new User();
		newUser.setUserId(userService.generateNextUserId());
		newUser.setRoleId(assignedRoleId);
		newUser.setEmployeeId(empId != null && !empId.isEmpty() ? empId.trim() : userService.generateNextEmployeeId());
		newUser.setUsername(username);
		newUser.setFullName(username);
		newUser.setEmail(email);
		newUser.setMobileNumber(phone);
		newUser.setStatus("ACTIVE");
		newUser.setUserCreatedAt(new Timestamp(System.currentTimeMillis()));

		boolean success = userService.registerOrUpdateUser(newUser, password);
		if (success) {
			AuditServiceImpl.getInstance().log("USER_MGMT", "CREATE_USER", 
					"Created user: " + username + " (Emp ID: " + newUser.getEmployeeId() + ", Role: " + roleDisplayName + ")", "SUCCESS");

			Clients.showNotification("User " + username + " created successfully!", "info", null, "top_center", 2500);
			loadUserData();
			switchView("LIST");
		} else {
			AuditServiceImpl.getInstance().log("USER_MGMT", "CREATE_USER_FAILED", 
					"Failed to register user: " + username + " (Emp ID: " + newUser.getEmployeeId() + ")", "FAILED");

			Clients.showNotification("Failed to save user in database. Ensure Employee ID or Username is not duplicated.", "error", null, "top_center", 3000);
		}
	}

	public void onClick$btnClearAddForm(Event event) {
		txtAddUsername.setValue("");
		txtAddEmail.setValue("");
		txtAddPhone.setValue("");
		txtAddPassword.setValue("");
		cmbAddRole.setValue(null);
	}

	public void onClick$btnCancelAdd(Event event) {
		switchView("LIST");
	}

	// --- SCREEN 3: MODIFY USER VIEW ---

	private void openModifyView(User user) {
		this.currentModUser = user;
		txtModEmployeeId.setValue(user.getEmployeeId());
		txtModUsername.setValue(user.getUsername());
		txtModEmail.setValue(user.getEmail());
		txtModCurrentRole.setValue(getRoleDisplayName(user.getRoleId()));

		// 1. Determine active status from DB record
		String status = user.getStatus();
		this.modUserActiveState = "ACTIVE".equalsIgnoreCase(status);
		syncStatusUI();

		// 2. Populate available roles excluding current role
		refreshRoleCache();
		cmbNewRole.getChildren().clear();
		for (Map.Entry<String, String> entry : roleMap.entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(user.getRoleId())) {
				Comboitem item = new Comboitem(entry.getValue());
				item.setValue(entry.getKey());
				cmbNewRole.appendChild(item);
			}
		}

		// 3. Reset Role picker: hidden and unselected by default
		chkChangeRole.setChecked(false);
		divNewRoleContainer.setVisible(false);
		cmbNewRole.setSelectedIndex(-1);
		cmbNewRole.setValue("");
		cmbNewRole.close();

		switchView("MODIFY");
	}

	private void syncStatusUI() {
		if (modUserActiveState) {
			lblModStatusText.setValue("ACTIVE");
			badgeUserStatus.setSclass("cts-status-badge cts-status-badge-active");
			if (dotUserStatus != null) {
				dotUserStatus.setSclass("cts-status-dot-active");
			}
			btnToggleUserStatus.setLabel("DEACTIVATE USER"); // or "DISABLE USER"
			btnToggleUserStatus.setSclass("cts-btn-status-toggle cts-btn-status-disable");
		} else {
			lblModStatusText.setValue("INACTIVE");
			badgeUserStatus.setSclass("cts-status-badge cts-status-badge-inactive");
			if (dotUserStatus != null) {
				dotUserStatus.setSclass("cts-status-dot-inactive");
			}
			btnToggleUserStatus.setLabel("ACTIVATE USER"); // or "ENABLE USER"
			btnToggleUserStatus.setSclass("cts-btn-status-toggle cts-btn-status-enable");
		}
	}

	public void onClick$btnToggleUserStatus(Event event) {
		this.modUserActiveState = !this.modUserActiveState;
		syncStatusUI();
	}

	public void onCheck$chkChangeRole(Event event) {
		boolean isChecked = chkChangeRole.isChecked();
		divNewRoleContainer.setVisible(isChecked);
		if (!isChecked) {
			cmbNewRole.setSelectedIndex(-1);
			cmbNewRole.setValue("");
		}
	}

	public void onClick$btnSaveModifications(Event event) {
		if (currentModUser == null) return;

		String newStatus = modUserActiveState ? "ACTIVE" : "INACTIVE";
		boolean statusChanged = !newStatus.equalsIgnoreCase(currentModUser.getStatus());

		boolean roleChanged = false;
		String newRoleId = currentModUser.getRoleId();
		String newRoleName = "";

		if (chkChangeRole.isChecked()) {
			Comboitem selectedItem = cmbNewRole.getSelectedItem();
			if (selectedItem == null || selectedItem.getValue() == null) {
				Clients.showNotification("Please select a new role or uncheck 'Reassign user's role'.", "error", cmbNewRole, "top_center", 2500);
				cmbNewRole.focus();
				return;
			}
			newRoleId = (String) selectedItem.getValue();
			newRoleName = selectedItem.getLabel();
			roleChanged = !newRoleId.equalsIgnoreCase(currentModUser.getRoleId());
		}

		if (!statusChanged && !roleChanged) {
			Clients.showNotification("No modifications were made.", "info", null, "top_center", 2000);
			switchView("LIST");
			return;
		}

		// Apply changes to current user object
		currentModUser.setStatus(newStatus);
		if (roleChanged) {
			currentModUser.setRoleId(newRoleId);
		}

		boolean saved = userService.registerOrUpdateUser(currentModUser, null);
		if (saved) {
			StringBuilder auditMsg = new StringBuilder("Updated user: ").append(currentModUser.getUsername());
			if (statusChanged) auditMsg.append(" | Status -> ").append(newStatus);
			if (roleChanged) auditMsg.append(" | Role -> ").append(newRoleName);

			AuditServiceImpl.getInstance().log("USER_MGMT", "MODIFY_USER", auditMsg.toString(), "SUCCESS");
			Clients.showNotification("User " + currentModUser.getUsername() + " updated successfully!", "info", null, "top_center", 2500);
			loadUserData();
			switchView("LIST");
		} else {
			AuditServiceImpl.getInstance().log("USER_MGMT", "MODIFY_USER_FAILED", 
					"Failed to update user: " + currentModUser.getUsername() + " (" + currentModUser.getEmployeeId() + ")", "FAILED");
			Clients.showNotification("Failed to update user in database.", "error", null, "top_center", 2500);
		}
	}

	public void onClick$btnCancelModifications(Event event) {
		switchView("LIST");
	}
}
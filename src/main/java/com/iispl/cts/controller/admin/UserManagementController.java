package com.iispl.cts.controller.admin;

import com.iispl.cts.entity.User;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.UserServiceImpl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserManagementController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // View Containers & Shared Header
    private Div viewUserList;
    private Div viewAddUser;
    private Div viewModifyUser;
    private Include incHeader;

    // View 1 (List) Controls
    private Textbox txtSearchQuery;
    private Combobox cmbRoleFilter;
    private Combobox cmbStatusFilter;
    private Label lblUserCount;
    private Label lblPaginationText;
    private Rows rowsUsers;
    private Button btnSearch;
    private Button btnClearFilter;
    private Button btnAddUser;

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
    private Div divNewRoleContainer;
    private Combobox cmbNewRole;
    private Button btnSaveModifications;
    private Button btnCancelModifications;
    private String selectedModifyAction = "CHANGE_ROLE";
    private User currentModUser;

    private final UserService userService = UserServiceImpl.getInstance();

    private static final Map<String, String> ROLE_MAP = new LinkedHashMap<>();
    static {
        ROLE_MAP.put("ROL1004", "Maker Inward");
        ROLE_MAP.put("ROL1002", "Maker Outward");
        ROLE_MAP.put("ROL1005", "Checker Inward");
        ROLE_MAP.put("ROL1003", "Checker Outward");
        ROLE_MAP.put("ROL1001", "Admin");
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        if (cmbRoleFilter != null && cmbRoleFilter.getItemCount() > 0) cmbRoleFilter.setSelectedIndex(0);
        if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) cmbStatusFilter.setSelectedIndex(0);
        loadUserData();
        switchView("LIST");
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
        return ROLE_MAP.getOrDefault(roleId, roleId);
    }

    // --- SCREEN 1: LIST VIEW ---
    private void loadUserData() {
        String query = txtSearchQuery != null ? txtSearchQuery.getValue() : "";
        String roleId = (cmbRoleFilter != null && cmbRoleFilter.getSelectedItem() != null)
                ? (String) cmbRoleFilter.getSelectedItem().getValue() : "ALL";
        String status = (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null)
                ? (String) cmbStatusFilter.getSelectedItem().getValue() : "ALL";

        List<User> users = userService.searchUsers(query, roleId, status);
        renderUserRows(users);
    }

    private void renderUserRows(List<User> users) {
        if (rowsUsers == null) return;
        rowsUsers.getChildren().clear();

        int count = users.size();
        if (lblUserCount != null) lblUserCount.setValue(count + " users found");
        if (lblPaginationText != null) lblPaginationText.setValue("Showing 1–" + count + " of " + count + " users");

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
            lblRole.setStyle("color: #4a5568; font-size: 13px;");
            row.appendChild(lblRole);

            Label lblStatus = new Label("ACTIVE".equalsIgnoreCase(user.getStatus()) ? "Active" : "Inactive");
            lblStatus.setStyle("ACTIVE".equalsIgnoreCase(user.getStatus())
                    ? "color: #2e7d32; font-weight: 700; font-size: 12px;"
                    : "color: #c62828; font-weight: 700; font-size: 12px;");
            row.appendChild(lblStatus);

            Button btnModify = new Button("MODIFY");
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

    public void onClick$btnSearch(Event event) { loadUserData(); }

    public void onClick$btnClearFilter(Event event) {
        if (txtSearchQuery != null) txtSearchQuery.setValue("");
        if (cmbRoleFilter != null) cmbRoleFilter.setSelectedIndex(0);
        if (cmbStatusFilter != null) cmbStatusFilter.setSelectedIndex(0);
        loadUserData();
    }

    // --- SCREEN 2: ADD USER VIEW ---
    public void onClick$btnAddUser(Event event) {
        txtAddEmployeeId.setValue(userService.generateNextEmployeeId());
        txtAddUsername.setValue("");
        txtAddEmail.setValue("");
        txtAddPhone.setValue("");
        txtAddPassword.setValue("");
        cmbAddRole.setValue(null);
        isPasswordVisible = false;
        txtAddPassword.setType("password");
        btnTogglePassword.setLabel("Show");

        switchView("ADD");
    }

    public void onClick$btnTogglePassword(Event event) {
        isPasswordVisible = !isPasswordVisible;
        txtAddPassword.setType(isPasswordVisible ? "text" : "password");
        btnTogglePassword.setLabel(isPasswordVisible ? "Hide" : "Show");
    }

    public void onClick$btnSubmitAddUser(Event event) {
        String empId = txtAddEmployeeId.getValue();
        String username = txtAddUsername.getValue();
        String email = txtAddEmail.getValue();
        String phone = txtAddPhone.getValue();
        String password = txtAddPassword.getValue();
        Comboitem selectedRole = cmbAddRole.getSelectedItem();

        if (username == null || username.trim().isEmpty()) {
            Clients.showNotification("Username is required.", "error", txtAddUsername, "top_center", 2000);
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            Clients.showNotification("Email is required.", "error", txtAddEmail, "top_center", 2000);
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            Clients.showNotification("Password is required.", "error", txtAddPassword, "top_center", 2000);
            return;
        }
        if (selectedRole == null) {
            Clients.showNotification("Please select a role.", "error", cmbAddRole, "top_center", 2000);
            return;
        }

        User newUser = new User();
        newUser.setUserId(userService.generateNextUserId());
        newUser.setRoleId((String) selectedRole.getValue());
        newUser.setEmployeeId(empId);
        newUser.setUsername(username.trim());
        newUser.setFullName(username.trim());
        newUser.setEmail(email.trim());
        newUser.setMobileNumber(phone != null ? phone.trim() : "");
        newUser.setStatus("ACTIVE");
        newUser.setUserCreatedAt(new Timestamp(System.currentTimeMillis()));

        boolean success = userService.registerOrUpdateUser(newUser, password.trim());
        if (success) {
            Clients.showNotification("User " + username + " (" + empId + ") added to database!", "info", null, "top_center", 2500);
            loadUserData();
            switchView("LIST");
        } else {
            Clients.showNotification("Failed to save user in database.", "error", null, "top_center", 2500);
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

        // Populate new roles dropdown excluding the user's current role
        cmbNewRole.getChildren().clear();
        for (Map.Entry<String, String> entry : ROLE_MAP.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(user.getRoleId())) {
                Comboitem item = new Comboitem(entry.getValue());
                item.setValue(entry.getKey());
                cmbNewRole.appendChild(item);
            }
        }

        // Default action to empty/none when opening the page
        this.selectedModifyAction = "";
        updateModifyActionStyles();
        switchView("MODIFY");
    }

    public void onClick$btnActionEnable(Event event) {
        this.selectedModifyAction = "ENABLE";
        updateModifyActionStyles();
    }

    public void onClick$btnActionDisable(Event event) {
        this.selectedModifyAction = "DISABLE";
        updateModifyActionStyles();
    }

    public void onClick$btnActionChangeRole(Event event) {
        this.selectedModifyAction = "CHANGE_ROLE";
        updateModifyActionStyles();
    }

    private void updateModifyActionStyles() {
    	btnActionEnable.setSclass("ENABLE".equals(selectedModifyAction) ? "cts-segment-btn cts-segment-btn-active" : "cts-segment-btn");
        btnActionDisable.setSclass("DISABLE".equals(selectedModifyAction) ? "cts-segment-btn cts-segment-btn-active" : "cts-segment-btn");
        btnActionChangeRole.setSclass("CHANGE_ROLE".equals(selectedModifyAction) ? "cts-segment-btn cts-segment-btn-active" : "cts-segment-btn");

        // 2. Show combobox container ONLY if CHANGE_ROLE is selected
        if (divNewRoleContainer != null) {
            divNewRoleContainer.setVisible("CHANGE_ROLE".equals(selectedModifyAction));
        }
    }

    public void onClick$btnSaveModifications(Event event) {
    	if (currentModUser == null) return;

        if (selectedModifyAction == null || selectedModifyAction.trim().isEmpty()) {
            Clients.showNotification("Please choose an action: ENABLE, DISABLE, or CHANGE ROLE.", "warning", null, "top_center", 2500);
            return;
        }

        if ("ENABLE".equals(selectedModifyAction)) {
            currentModUser.setStatus("ACTIVE");
        } else if ("DISABLE".equals(selectedModifyAction)) {
            currentModUser.setStatus("INACTIVE");
        } else if ("CHANGE_ROLE".equals(selectedModifyAction)) {
            Comboitem selectedItem = cmbNewRole.getSelectedItem();
            if (selectedItem == null) {
                Clients.showNotification("Please select a new role from the list.", "error", cmbNewRole, "top_center", 2500);
                return;
            }
            currentModUser.setRoleId((String) selectedItem.getValue());
        }

        boolean saved = userService.registerOrUpdateUser(currentModUser, null);
        if (saved) {
            Clients.showNotification("User " + currentModUser.getUsername() + " updated in database!", "info", null, "top_center", 2500);
            loadUserData();
            switchView("LIST");
        } else {
            Clients.showNotification("Failed to update user in database.", "error", null, "top_center", 2500);
        }
    }

    public void onClick$btnCancelModifications(Event event) {
        switchView("LIST");
    }
}
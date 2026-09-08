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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

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
    private Div viewUserList;
    private Div viewAddUser;
    private Div viewModifyUser;
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
    private Div divNewRoleContainer;
    private Combobox cmbNewRole;
    private Button btnSaveModifications;
    private Button btnCancelModifications;
    private String selectedModifyAction = "CHANGE_ROLE";
    private User currentModUser;

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

    public void onClick$btnAddUser(Event event) {
        txtAddEmployeeId.setValue(userService.generateNextEmployeeId());
        txtAddUsername.setValue("");
        txtAddEmail.setValue("");
        txtAddPhone.setValue("");
        txtAddPassword.setValue("");
        isPasswordVisible = false;
        txtAddPassword.setType("password");
        btnTogglePassword.setLabel("Show");

        refreshRoleCache();
        populateAddRoleDropdown();
        cmbAddRole.setValue(null);

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

        // Duplicate username pre-check
        if (userService.findByUsername(username.trim()) != null) {
            Clients.showNotification("Username '" + username.trim() + "' already exists.", "error", txtAddUsername, "top_center", 3000);
            return;
        }

        String assignedRoleId = (String) selectedRole.getValue();
        String roleDisplayName = getRoleDisplayName(assignedRoleId);

        User newUser = new User();
        newUser.setUserId(userService.generateNextUserId());
        newUser.setRoleId(assignedRoleId);
        newUser.setEmployeeId(empId != null ? empId.trim() : userService.generateNextEmployeeId());
        newUser.setUsername(username.trim());
        newUser.setFullName(username.trim());
        newUser.setEmail(email.trim());
        newUser.setMobileNumber(phone != null ? phone.trim() : "");
        newUser.setStatus("ACTIVE");
        newUser.setUserCreatedAt(new Timestamp(System.currentTimeMillis()));

        boolean success = userService.registerOrUpdateUser(newUser, password.trim());
        if (success) {
            AuditServiceImpl.getInstance().log("USER_MGMT", "CREATE_USER", 
                "Created user: " + username.trim() + " (Emp ID: " + newUser.getEmployeeId() + ", Role: " + roleDisplayName + ")", "SUCCESS");

            Clients.showNotification("User " + username.trim() + " created successfully!", "info", null, "top_center", 2500);
            loadUserData();
            switchView("LIST");
        } else {
            AuditServiceImpl.getInstance().log("USER_MGMT", "CREATE_USER_FAILED", 
                "Failed to register user: " + username.trim() + " (Emp ID: " + newUser.getEmployeeId() + ")", "FAILED");

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

        refreshRoleCache();
        cmbNewRole.getChildren().clear();
        for (Map.Entry<String, String> entry : roleMap.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(user.getRoleId())) {
                Comboitem item = new Comboitem(entry.getValue());
                item.setValue(entry.getKey());
                cmbNewRole.appendChild(item);
            }
        }

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

        String auditAction = "";
        String auditDetail = "";

        if ("ENABLE".equals(selectedModifyAction)) {
            currentModUser.setStatus("ACTIVE");
            auditAction = "ENABLE_USER";
            auditDetail = "Enabled user account: " + currentModUser.getUsername();
        } else if ("DISABLE".equals(selectedModifyAction)) {
            currentModUser.setStatus("INACTIVE");
            auditAction = "DISABLE_USER";
            auditDetail = "Disabled user account: " + currentModUser.getUsername();
        } else if ("CHANGE_ROLE".equals(selectedModifyAction)) {
            Comboitem selectedItem = cmbNewRole.getSelectedItem();
            if (selectedItem == null) {
                Clients.showNotification("Please select a new role from the list.", "error", cmbNewRole, "top_center", 2500);
                return;
            }
            currentModUser.setRoleId((String) selectedItem.getValue());
            auditAction = "CHANGE_ROLE";
            auditDetail = "Changed role of user " + currentModUser.getUsername() + " to " + selectedItem.getLabel();
        }

        boolean saved = userService.registerOrUpdateUser(currentModUser, null);
        if (saved) {
            AuditServiceImpl.getInstance().log("USER_MGMT", auditAction, auditDetail, "SUCCESS");
            Clients.showNotification("User " + currentModUser.getUsername() + " updated in database!", "info", null, "top_center", 2500);
            loadUserData();
            switchView("LIST");
        } else {
            AuditServiceImpl.getInstance().log("USER_MGMT", auditAction + "_FAILED", 
                "Failed to update user: " + currentModUser.getUsername() + " (" + currentModUser.getEmployeeId() + ")", "FAILED");
            Clients.showNotification("Failed to update user in database.", "error", null, "top_center", 2500);
        }
    }

    public void onClick$btnCancelModifications(Event event) {
        switchView("LIST");
    }
}
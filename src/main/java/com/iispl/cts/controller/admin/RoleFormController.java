package com.iispl.cts.controller.admin;

import java.sql.Timestamp;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.entity.Role;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;
import com.iispl.cts.serviceimpl.RoleServiceImpl;
import com.iispl.cts.serviceimpl.UserServiceImpl;

public class RoleFormController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Form inputs (Matched by ID automatically by GenericForwardComposer)
    private Textbox txtRoleId;
    private Textbox txtRoleName;
    private Textbox txtDescription;
    private Combobox cmbStatus;
    private Listbox lstAssignedUsers;

    private Button btnSave;
    private Button btnCancel;
    private Window winConfirmModal;
    
    // Status Badge UI components (used in Modify Role)
    private Hlayout boxStatusBadge;
    private Div dotStatus;
    private Label lblCurrentStatus;
    private Button btnToggleStatus;
    
    private final UserService userService = new UserServiceImpl();
    private final RoleService roleService = RoleServiceImpl.getInstance();
    
    private String roleIdParam;
    private boolean isModifyMode = false;
    private boolean isRoleActive = true;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        if (!SecurityUtil.checkAccess(null)) {
            return;
        }
        super.doAfterCompose(comp);
        
        // Attach listeners directly to the modal buttons
        if (winConfirmModal != null) {
            Button btnCancelModal = (Button) winConfirmModal.getFellow("btnCancelModal");
            btnCancelModal.addEventListener("onClick", event -> {
                winConfirmModal.setVisible(false);
            });

            Button btnConfirmAction = (Button) winConfirmModal.getFellow("btnConfirmAction");
            btnConfirmAction.addEventListener("onClick", event -> {
                winConfirmModal.setVisible(false);
                processSaveRole();
            });
        }

        roleIdParam = Executions.getCurrent().getParameter("roleId");
        if (roleIdParam == null || roleIdParam.trim().isEmpty()) {
            Object argVal = Executions.getCurrent().getArg().get("roleId");
            if (argVal != null) {
                roleIdParam = String.valueOf(argVal);
            }
        }

        if (roleIdParam != null && !roleIdParam.trim().isEmpty()) {
            isModifyMode = true;
            loadRoleForEdit(roleIdParam.trim());
        } else {
            isModifyMode = false;
            String nextId = roleService.generateNextRoleId();
            if (txtRoleId != null) {
                txtRoleId.setValue(nextId);
            }
            if (cmbStatus != null && cmbStatus.getItemCount() > 0) {
                cmbStatus.setSelectedIndex(0);
            }
            this.isRoleActive = true;
            updateStatusDisplay();
        }
    }

    private void loadRoleForEdit(String id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            Messagebox.show("Role not found!", "Error", Messagebox.OK, Messagebox.ERROR, e -> {
                Executions.sendRedirect("/admin/role/role-management.zul");
            });
            return;
        }

        if (txtRoleId != null) txtRoleId.setValue(role.getRoleId());
        if (txtRoleName != null) txtRoleName.setValue(role.getRoleName());
        if (txtDescription != null) txtDescription.setValue(role.getDescription());

        // Sync combobox if present
        if (cmbStatus != null) {
            for (Comboitem item : cmbStatus.getItems()) {
                if (item.getLabel().equalsIgnoreCase(role.getStatus())) {
                    cmbStatus.setSelectedItem(item);
                    break;
                }
            }
        }

        // Sync badge state if present
        this.isRoleActive = "ACTIVE".equalsIgnoreCase(role.getStatus());
        updateStatusDisplay();

        // Load the assigned users for this role
        populateAssignedUsers(id);
    }

    // Toggle button listener for modify-role.zul
    public void onClick$btnToggleStatus(Event event) {
        this.isRoleActive = !this.isRoleActive;
        updateStatusDisplay();
    }

    private void updateStatusDisplay() {
        if (lblCurrentStatus == null) return;

        if (isRoleActive) {
            if (boxStatusBadge != null) {
                boxStatusBadge.setStyle("background-color: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 9999px; padding: 6px 14px;");
            }
            if (dotStatus != null) {
                dotStatus.setStyle("width: 8px; height: 8px; border-radius: 50%; background-color: #059669;");
            }
            lblCurrentStatus.setValue("ACTIVE");
            lblCurrentStatus.setStyle("color: #065f46; font-size: 13px; font-weight: 700; letter-spacing: 0.5px; line-height: 1;");

            if (btnToggleStatus != null) {
                btnToggleStatus.setLabel("DEACTIVATE ROLE");
                btnToggleStatus.setStyle("background: #ffffff; border: 1px solid #fca5a5; color: #dc2626; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; padding: 6px 14px; border-radius: 6px; cursor: pointer; height: 32px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);");
            }
        } else {
            if (boxStatusBadge != null) {
                boxStatusBadge.setStyle("background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 9999px; padding: 6px 14px;");
            }
            if (dotStatus != null) {
                dotStatus.setStyle("width: 8px; height: 8px; border-radius: 50%; background-color: #dc2626;");
            }
            lblCurrentStatus.setValue("INACTIVE");
            lblCurrentStatus.setStyle("color: #991b1b; font-size: 13px; font-weight: 700; letter-spacing: 0.5px; line-height: 1;");

            if (btnToggleStatus != null) {
                btnToggleStatus.setLabel("ACTIVATE ROLE");
                btnToggleStatus.setStyle("background: #ffffff; border: 1px solid #86efac; color: #16a34a; font-size: 12px; font-weight: 700; letter-spacing: 0.5px; padding: 6px 14px; border-radius: 6px; cursor: pointer; height: 32px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);");
            }
        }
    }

    private String getResolvedStatus() {
        if (lblCurrentStatus != null) {
            return isRoleActive ? "Active" : "Inactive";
        } else if (cmbStatus != null && cmbStatus.getSelectedItem() != null) {
            return cmbStatus.getSelectedItem().getLabel();
        }
        return "Active";
    }

    private void populateAssignedUsers(String roleId) {
        if (lstAssignedUsers == null) {
            return;
        }

        lstAssignedUsers.getItems().clear();

        List<User> users = userService.findUsersByRoleId(roleId);

        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                Listitem item = new Listitem();

                // 1. Employee ID
                String empId = (user.getEmployeeId() != null && !user.getEmployeeId().isEmpty()) 
                               ? user.getEmployeeId() : user.getUserId();
                item.appendChild(new Listcell(empId));

                // 2. Operator Name
                item.appendChild(new Listcell(user.getFullName() != null ? user.getFullName() : "-"));

                // 3. Username
                item.appendChild(new Listcell(user.getUsername() != null ? user.getUsername() : "-"));

                // 4. Email
                item.appendChild(new Listcell(user.getEmail() != null ? user.getEmail() : "-"));

                // 5. Mobile (Center aligned)
                Listcell mobileCell = new Listcell(user.getMobileNumber() != null ? user.getMobileNumber() : "-");
                mobileCell.setStyle("text-align: center;");
                item.appendChild(mobileCell);

                // 6. Status (Center aligned badge)
                Listcell statusCell = new Listcell();
                statusCell.setStyle("text-align: center;");

                boolean isActive = "ACTIVE".equalsIgnoreCase(user.getStatus());

                org.zkoss.zul.Label lblStatus = new org.zkoss.zul.Label(isActive ? "Active" : "Inactive");
                lblStatus.setSclass(isActive ? "badge-status badge-active" : "badge-status badge-inactive");

                statusCell.appendChild(lblStatus);
                item.appendChild(statusCell);

                lstAssignedUsers.appendChild(item);
            }
        }
    }

    public void onClick$btnSave(Event event) {
        String roleId = (txtRoleId != null && txtRoleId.getValue() != null) ? txtRoleId.getValue().trim() : "";
        String roleName = (txtRoleName != null && txtRoleName.getValue() != null) ? txtRoleName.getValue().trim() : "";
        String desc = (txtDescription != null && txtDescription.getValue() != null) ? txtDescription.getValue().trim() : "";
        String status = getResolvedStatus();

        // 1. Validations
        if (roleName.isEmpty()) {
            Clients.showNotification("Please enter Role Name.", "error", txtRoleName, "end_center", 3000);
            if (txtRoleName != null) txtRoleName.focus();
            return;
        }

        if (roleName.length() < 3 || roleName.length() > 50) {
            Clients.showNotification("Role Name must be between 3 and 50 characters.", "error", txtRoleName, "end_center", 3000);
            if (txtRoleName != null) txtRoleName.focus();
            return;
        }

        if (!roleName.matches("^[a-zA-Z0-9 _-]+$")) {
            Clients.showNotification("Only letters, numbers, spaces, hyphens, and underscores allowed.", "error", txtRoleName, "end_center", 3000);
            if (txtRoleName != null) txtRoleName.focus();
            return;
        }

        if (desc.isEmpty()) {
            Clients.showNotification("Please enter Role Description.", "error", txtDescription, "end_center", 3000);
            if (txtDescription != null) txtDescription.focus();
            return;
        }

        // Duplicate Check
        if (!isModifyMode) {
            if (roleService.isRoleNameExists(roleName)) {
                Clients.showNotification("A role named '" + roleName + "' already exists.", "error", txtRoleName, "end_center", 3500);
                if (txtRoleName != null) txtRoleName.focus();
                return;
            }
        } else {
            if (roleService.isRoleNameExists(roleName, roleId)) {
                Clients.showNotification("Another role with the name '" + roleName + "' already exists.", "error", txtRoleName, "end_center", 3500);
                if (txtRoleName != null) txtRoleName.focus();
                return;
            }
        }

        // 2. Fetch components safely via winConfirmModal.getFellow(...)
        Label lblConfirmRoleId = (Label) winConfirmModal.getFellow("lblConfirmRoleId");
        Label lblConfirmRoleName = (Label) winConfirmModal.getFellow("lblConfirmRoleName");
        Label lblConfirmDesc = (Label) winConfirmModal.getFellow("lblConfirmDesc");
        Label lblConfirmStatus = (Label) winConfirmModal.getFellow("lblConfirmStatus");
        Label lblModalTitle = (Label) winConfirmModal.getFellow("lblModalTitle");
        Button btnConfirmAction = (Button) winConfirmModal.getFellow("btnConfirmAction");

        lblConfirmRoleId.setValue(roleId);
        lblConfirmRoleName.setValue(roleName);
        lblConfirmDesc.setValue(desc);
        lblConfirmStatus.setValue(status);

        if (isModifyMode) {
            lblModalTitle.setValue("Confirm Role Update");
            btnConfirmAction.setLabel("Update Role");
        } else {
            lblModalTitle.setValue("Confirm New Role");
            btnConfirmAction.setLabel("Create Role");
        }

        // Show modal popup
        winConfirmModal.doModal();
    }

    private void processSaveRole() {
        String roleId = (txtRoleId != null && txtRoleId.getValue() != null) ? txtRoleId.getValue().trim() : "";
        String roleName = (txtRoleName != null && txtRoleName.getValue() != null) ? txtRoleName.getValue().trim() : "";
        String desc = (txtDescription != null && txtDescription.getValue() != null) ? txtDescription.getValue().trim() : "";
        String status = getResolvedStatus();

        Role role = new Role(roleId, roleName, desc, status, null, new Timestamp(System.currentTimeMillis()));

        boolean success;
        if (isModifyMode) {
            success = roleService.updateRole(role);
            if (success) {
                AuditServiceImpl.getInstance().log("ROLE_MGMT", "MODIFY_ROLE", 
                    "Updated role details for: " + roleName + " (" + roleId + ")", "SUCCESS");
            } else {
                AuditServiceImpl.getInstance().log("ROLE_MGMT", "MODIFY_ROLE_FAILED", 
                    "Failed to update role: " + roleName + " (" + roleId + ")", "FAILED");
            }
        } else {
            success = roleService.saveRole(role);
            if (success) {
                AuditServiceImpl.getInstance().log("ROLE_MGMT", "CREATE_ROLE", 
                    "Created new role: " + roleName + " (" + roleId + ")", "SUCCESS");
            } else {
                AuditServiceImpl.getInstance().log("ROLE_MGMT", "CREATE_ROLE_FAILED", 
                    "Failed to create role: " + roleName + " (" + roleId + ")", "FAILED");
            }
        }

        if (success) {
            Messagebox.show("Role " + (isModifyMode ? "updated" : "created") + " successfully.", "Success", Messagebox.OK, Messagebox.INFORMATION, e -> {
                Executions.sendRedirect("/admin/role/role-management.zul");
            });
        } else {
            Messagebox.show("Failed to save role. Please verify input.", "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    public void onClick$btnCancel(Event event) {
        Executions.sendRedirect("/admin/role/role-management.zul");
    }
}
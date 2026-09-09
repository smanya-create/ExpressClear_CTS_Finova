package com.iispl.cts.controller.admin;

import java.sql.Timestamp;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

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

    private final UserService userService = new UserServiceImpl();
    private final RoleService roleService = RoleServiceImpl.getInstance();
    
    private String roleIdParam;
    private boolean isModifyMode = false;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
    	if(!SecurityUtil.checkAccess(null)) {
    		return;
    	}
        super.doAfterCompose(comp);

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

        if (cmbStatus != null) {
            for (Comboitem item : cmbStatus.getItems()) {
                if (item.getLabel().equalsIgnoreCase(role.getStatus())) {
                    cmbStatus.setSelectedItem(item);
                    break;
                }
            }
        }

        // LOAD THE ASSIGNED USERS FOR THIS ROLE
        populateAssignedUsers(id);
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

                String empId = (user.getEmployeeId() != null && !user.getEmployeeId().isEmpty()) 
                               ? user.getEmployeeId() : user.getUserId();
                item.appendChild(new Listcell(empId));
                item.appendChild(new Listcell(user.getFullName() != null ? user.getFullName() : "-"));
                item.appendChild(new Listcell(user.getUsername() != null ? user.getUsername() : "-"));
                item.appendChild(new Listcell(user.getEmail() != null ? user.getEmail() : "-"));
                item.appendChild(new Listcell(user.getMobileNumber() != null ? user.getMobileNumber() : "-"));

                Listcell statusCell = new Listcell();
                boolean isActive = "ACTIVE".equalsIgnoreCase(user.getStatus());
                statusCell.setLabel(isActive ? "Active" : "Inactive");
                statusCell.setStyle(isActive ? "color: #16a34a; font-weight: 700;" : "color: #dc2626; font-weight: 700;");
                item.appendChild(statusCell);

                lstAssignedUsers.appendChild(item);
            }
        }
    }

    public void onClick$btnSave(Event event) {
        String roleId = (txtRoleId != null && txtRoleId.getValue() != null) ? txtRoleId.getValue().trim() : "";
        String roleName = (txtRoleName != null && txtRoleName.getValue() != null) ? txtRoleName.getValue().trim() : "";
        String desc = (txtDescription != null && txtDescription.getValue() != null) ? txtDescription.getValue().trim() : "";
        String status = (cmbStatus != null && cmbStatus.getSelectedItem() != null) ? cmbStatus.getSelectedItem().getLabel() : "Active";

        // 1. Basic empty validation
        if (roleName.isEmpty()) {
            Messagebox.show("Please enter Role Name.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // 2. Duplicate Role Name Validation
        if (!isModifyMode) {
            // Mode: ADD ROLE - Check if the role name already exists anywhere
            if (roleService.isRoleNameExists(roleName)) {
                Messagebox.show("A role named '" + roleName + "' already exists.", "Duplicate Role", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
        } else {
            // Mode: MODIFY ROLE - Check if another role already uses this name (excluding current roleId)
            if (roleService.isRoleNameExists(roleName, roleId)) {
                Messagebox.show("Another role with the name '" + roleName + "' already exists.", "Duplicate Role", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
        }

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
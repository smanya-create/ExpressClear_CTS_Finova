package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.Role;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;
import com.iispl.cts.serviceimpl.RoleServiceImpl;
import com.iispl.cts.serviceimpl.UserServiceImpl;

public class LoginController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Component wires
    private Textbox txtUsername;
    private Textbox txtPassword;
    private Combobox cmbRole;
    private Button btnSignIn;

    private final UserService userService = UserServiceImpl.getInstance();
    private final RoleService roleService = RoleServiceImpl.getInstance();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        populateRolesDropdown();
    }

    /**
     * Loads roles from DB or loads standard defaults if empty
     */
    private void populateRolesDropdown() {
        if (cmbRole == null) return;
        cmbRole.getChildren().clear();

        try {
            List<Role> roles = roleService.getAllRoles();
            if (roles != null && !roles.isEmpty()) {
                for (Role r : roles) {
                    Comboitem item = new Comboitem(r.getRoleName());
                    item.setValue(r.getRoleId());
                    cmbRole.appendChild(item);
                }
            } else {
                Comboitem itm1 = new Comboitem("Admin"); itm1.setValue("ROL1001"); cmbRole.appendChild(itm1);
                Comboitem itm2 = new Comboitem("Maker Outward"); itm2.setValue("ROL1002"); cmbRole.appendChild(itm2);
                Comboitem itm3 = new Comboitem("Checker Outward"); itm3.setValue("ROL1003"); cmbRole.appendChild(itm3);
                Comboitem itm4 = new Comboitem("Maker Inward"); itm4.setValue("ROL1004"); cmbRole.appendChild(itm4);
                Comboitem itm5 = new Comboitem("Checker Inward"); itm5.setValue("ROL1005"); cmbRole.appendChild(itm5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onClick$btnSignIn() {
        processLogin();
    }
   
    public void onClick$btnSignIn(Event event) {
        processLogin();
    }

    public void onOK$txtUsername(Event event) {
        if (txtPassword != null) {
            txtPassword.setFocus(true);
        }
    }

    public void onOK$txtPassword(Event event) {
        processLogin();
    }

    private void processLogin() {
        String username = (txtUsername != null && txtUsername.getValue() != null) ? txtUsername.getValue().trim() : "";
        String password = (txtPassword != null && txtPassword.getValue() != null) ? txtPassword.getValue().trim() : "";
        Comboitem selectedItem = (cmbRole != null) ? cmbRole.getSelectedItem() : null;
        

        if (username.isEmpty() || password.isEmpty() || selectedItem == null) {
            Messagebox.show("Please enter username, password, and select a role.", 
                            "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        String selectedRoleId = selectedItem.getValue() != null ? selectedItem.getValue().toString().trim() : "";
        String selectedRoleName = selectedItem.getLabel() != null ? selectedItem.getLabel().trim() : "";

        // 1. Authenticate user credentials
        User user = userService.authenticate(username, password);
        if (user == null) {
        	AuditServiceImpl.getInstance().log("AUTH", "LOGIN_FAILED", 
                    "Invalid username or password attempt for username: " + username, "FAILED");

                Messagebox.show("Invalid username or password.", "Authentication Failed", Messagebox.OK, Messagebox.ERROR);
                return;
        }

        // 2. Fetch User's Role details
        String userRoleId = user.getRoleId() != null ? user.getRoleId().trim() : "";
        Role userRole = roleService.getRoleById(userRoleId);
        
        String userPermissions = (userRole != null && userRole.getPermissions() != null) 
                ? userRole.getPermissions().trim() 
                : "";

        // Fallback role name if Role entity lookup is null
        String dbRoleName = "";
        if (userRole != null && userRole.getRoleName() != null) {
            dbRoleName = userRole.getRoleName().trim();
        } else {
            if ("ROL1001".equalsIgnoreCase(userRoleId)) dbRoleName = "Admin";
            else if ("ROL1002".equalsIgnoreCase(userRoleId)) dbRoleName = "Maker Outward";
            else if ("ROL1003".equalsIgnoreCase(userRoleId)) dbRoleName = "Checker Outward";
            else if ("ROL1004".equalsIgnoreCase(userRoleId)) dbRoleName = "Maker Inward";
            else if ("ROL1005".equalsIgnoreCase(userRoleId)) dbRoleName = "Checker Inward";
            else dbRoleName = selectedRoleName;
        }

        // 3. Verify assigned role against selected dropdown role
        boolean isRoleMatch = userRoleId.equalsIgnoreCase(selectedRoleId) ||
                              dbRoleName.equalsIgnoreCase(selectedRoleName) ||
                              dbRoleName.replace(" ", "_").equalsIgnoreCase(selectedRoleId) ||
                              selectedRoleName.equalsIgnoreCase(dbRoleName);

        if (!isRoleMatch) {
        	AuditServiceImpl.getInstance().log("AUTH", "LOGIN_ROLE_MISMATCH", 
                    "Access denied for user " + username + ": Assigned role (" + dbRoleName + ") does not match selected role (" + selectedRoleName + ")", "FAILED");

                Messagebox.show("Access Denied: Assigned role (" + dbRoleName + 
                                ") does not match selected role (" + selectedRoleName + ").", 
                                "Role Mismatch", Messagebox.OK, Messagebox.EXCLAMATION);
                return;
        }

        // 4. Normalize role token for session tracking
        String normalizedRole = dbRoleName.toUpperCase().replace(" ", "_");

        // 5. Store session attributes
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        Sessions.getCurrent().setAttribute("LOGGED_USER", user.getFullName());
        Sessions.getCurrent().setAttribute("USER_ID", user.getUserId());
        Sessions.getCurrent().setAttribute("USERNAME", user.getUsername());
        Sessions.getCurrent().setAttribute("CTS_USERNAME", user.getUsername());
        Sessions.getCurrent().setAttribute("USER_ROLE", normalizedRole);
        Sessions.getCurrent().setAttribute("CTS_USER_ROLE", normalizedRole);
        Sessions.getCurrent().setAttribute("ROLE_ID", userRoleId);
        Sessions.getCurrent().setAttribute("ROLE_NAME", dbRoleName);
        Sessions.getCurrent().setAttribute("USER_OBJ", user);
        Sessions.getCurrent().setAttribute("CLEARING_DATE", sdf.format(new Date()));
        Sessions.getCurrent().setAttribute("USER_PERMISSIONS", userPermissions);
        
        AuditServiceImpl.getInstance().log("AUTH", "LOGIN", 
                "User " + user.getUsername() + " logged in successfully with role " + dbRoleName, "SUCCESS");

        // 6. Direct Navigation by Role ID and Role Name
        if ("ROL1001".equalsIgnoreCase(userRoleId) || normalizedRole.contains("ADMIN")) {
            Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
        } 
        else if ("ROL1002".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/maker/maker-dashboard.zul");
        } 
        else if ("ROL1003".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/checker/dashboard.zul");
        } 
        else if ("ROL1004".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/maker/index.zul");
        } 
        else if ("ROL1005".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/checker/inward-checker-dashboard.zul");
        } 
        else {
            Messagebox.show("No dashboard mapped for role: " + dbRoleName + " (ID: " + userRoleId + ")", 
                            "Navigation Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}
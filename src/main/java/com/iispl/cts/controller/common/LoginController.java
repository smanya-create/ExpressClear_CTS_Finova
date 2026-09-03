package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.util.ActiveUserManager;
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
    private Button btnTogglePassword;

    private boolean isPasswordVisible = false;

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

    public void onClick$btnSignIn(Event event) {
        processLogin();
    }

    public void onTogglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            txtPassword.setType("text");
            if (btnTogglePassword != null) {
                btnTogglePassword.setIconSclass("z-icon-eye-slash");
            }
        } else {
            txtPassword.setType("password");
            if (btnTogglePassword != null) {
                btnTogglePassword.setIconSclass("z-icon-eye");
            }
        }
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
        final String username = (txtUsername != null && txtUsername.getValue() != null) ? txtUsername.getValue().trim() : "";
        final String password = (txtPassword != null && txtPassword.getValue() != null) ? txtPassword.getValue().trim() : "";
        Comboitem selectedItem = (cmbRole != null) ? cmbRole.getSelectedItem() : null;

        if (username.isEmpty() || password.isEmpty() || selectedItem == null) {
            Messagebox.show("Please enter username, password, and select a role.",
                            "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        final String selectedRoleId = selectedItem.getValue() != null ? selectedItem.getValue().toString().trim() : "";
        final String selectedRoleName = selectedItem.getLabel() != null ? selectedItem.getLabel().trim() : "";
        System.out.println("========== LOGIN PERFORMANCE BENCHMARK ==========");
        long tStart = System.currentTimeMillis();
        // 1. Authenticate user
        long t0 = System.currentTimeMillis();
        User authenticatedUser = userService.authenticate(username, password);
        long t1 = System.currentTimeMillis();
        System.out.println("LOG: BCrypt + User Lookup took: " + (t1 - t0) + " ms");

        if (authenticatedUser == null) {
            AuditServiceImpl.getInstance().log("AUTH", "LOGIN_FAILED",
                    "Invalid username or password attempt for username: " + username, "FAILED");
            Messagebox.show("Invalid username or password.", "Authentication Failed", Messagebox.OK, Messagebox.ERROR);
            return;
        }

        // 2. Fetch assigned role & permissions
        String userRoleId = authenticatedUser.getRoleId() != null ? authenticatedUser.getRoleId().trim() : "";
        Role userRole = roleService.getRoleById(userRoleId);
        long t2 = System.currentTimeMillis();
        System.out.println("LOG: Role Lookup took: " + (t2 - t1) + " ms");
        String userPermissions = (userRole != null && userRole.getPermissions() != null) ? userRole.getPermissions().trim() : "";

        String computedDbRoleName;
        if (userRole != null && userRole.getRoleName() != null) {
            computedDbRoleName = userRole.getRoleName().trim();
        } else {
            if ("ROL1001".equalsIgnoreCase(userRoleId)) computedDbRoleName = "Admin";
            else if ("ROL1002".equalsIgnoreCase(userRoleId)) computedDbRoleName = "Maker Outward";
            else if ("ROL1003".equalsIgnoreCase(userRoleId)) computedDbRoleName = "Checker Outward";
            else if ("ROL1004".equalsIgnoreCase(userRoleId)) computedDbRoleName = "Maker Inward";
            else if ("ROL1005".equalsIgnoreCase(userRoleId)) computedDbRoleName = "Checker Inward";
            else computedDbRoleName = selectedRoleName;
        }

        // 3. Verify selected role against database role
        boolean isRoleMatch = userRoleId.equalsIgnoreCase(selectedRoleId) ||
                              computedDbRoleName.equalsIgnoreCase(selectedRoleName) ||
                              computedDbRoleName.replace(" ", "_").equalsIgnoreCase(selectedRoleId) ||
                              selectedRoleName.equalsIgnoreCase(computedDbRoleName);

        if (!isRoleMatch) {
            AuditServiceImpl.getInstance().log("AUTH", "LOGIN_ROLE_MISMATCH",
                    "Access denied for user " + username + ": Assigned role (" + computedDbRoleName 
                    + ") does not match selected role (" + selectedRoleName + ")", "FAILED");
            Messagebox.show("Access Denied: Assigned role (" + computedDbRoleName +
                            ") does not match selected role (" + selectedRoleName + ").",
                            "Role Mismatch", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // 4. Bind Session Attributes
        Session session = Sessions.getCurrent();
        String normalizedRole = computedDbRoleName.toUpperCase().replace(" ", "_");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        session.setAttribute("LOGGED_USER", authenticatedUser.getFullName());
        session.setAttribute("USER_ID", authenticatedUser.getUserId());
        session.setAttribute("USERNAME", authenticatedUser.getUsername());
        session.setAttribute("CTS_USERNAME", authenticatedUser.getUsername());
        session.setAttribute("USER_ROLE", normalizedRole);
        session.setAttribute("CTS_USER_ROLE", normalizedRole);
        session.setAttribute("ROLE_ID", authenticatedUser.getRoleId());
        session.setAttribute("ROLE_NAME", computedDbRoleName);
        session.setAttribute("USER_OBJ", authenticatedUser);
        session.setAttribute("CLEARING_DATE", sdf.format(new Date()));
        session.setAttribute("USER_PERMISSIONS", userPermissions);

        // Register user in active user registry
        ActiveUserManager.userLoggedIn(authenticatedUser.getUserId());

        // Audit success log
        AuditServiceImpl.getInstance().log("AUTH", "LOGIN",
                "User " + authenticatedUser.getUsername() + " logged in successfully with role " + computedDbRoleName, "SUCCESS");
        long t3 = System.currentTimeMillis();
        System.out.println("3. Session Bind + Audit Trigger: " + (t3 - t2) + " ms");
        System.out.println("TOTAL LOGIN HANDSHAKE: " + (t3 - tStart) + " ms");

        // 5. Navigate user directly to dashboard
        if ("ROL1001".equalsIgnoreCase(userRoleId) || normalizedRole.contains("ADMIN")) {
            Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
        } else if ("ROL1002".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/maker/maker-module.zul");
        } else if ("ROL1003".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/checker/dashboard.zul");
        } else if ("ROL1004".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/maker/index.zul");
        } else if ("ROL1005".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/checker/dashboard.zul");
        } else {
            Messagebox.show("No dashboard mapped for role: " + computedDbRoleName + " (ID: " + userRoleId + ")",
                            "Navigation Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}
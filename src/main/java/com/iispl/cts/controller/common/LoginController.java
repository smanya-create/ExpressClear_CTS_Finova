package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
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
    private Button btnTogglePassword;

    private boolean isPasswordVisible = false;

    private final UserService userService = UserServiceImpl.getInstance();
    private final RoleService roleService = RoleServiceImpl.getInstance();

    // Reusable thread pool for login execution
    private static final ExecutorService loginExecutor = Executors.newFixedThreadPool(4);

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Desktop desktop = comp.getDesktop();
        if (desktop != null && !desktop.isServerPushEnabled()) {
            desktop.enableServerPush(true);
        }

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

        // 1. Immediate UI Feedback: Disable button and show busy indicator
        btnSignIn.setDisabled(true);
        btnSignIn.setLabel("Authenticating...");
        Clients.showBusy("Authenticating user credentials...");

        // 2. Capture ZK Desktop & Session on the UI Thread BEFORE dispatching
        final Desktop desktop = self.getDesktop();
        final Session session = Sessions.getCurrent();

        // 3. Heavy logic executed in background thread
        loginExecutor.submit(() -> {
            User authenticatedUser = null;
            Role userRole = null;
            String computedDbRoleName = "";
            String userPermissions = "";
            int errorType = 0; // 0 = Success, 1 = Invalid Creds, 2 = Role Mismatch, 3 = Exception

            try {
                // Heavy DB check: BCrypt / Credential validation
                authenticatedUser = userService.authenticate(username, password);

                if (authenticatedUser == null) {
                    AuditServiceImpl.getInstance().log("AUTH", "LOGIN_FAILED",
                            "Invalid username or password attempt for username: " + username, "FAILED");
                    errorType = 1;
                } else {
                    String userRoleId = authenticatedUser.getRoleId() != null ? authenticatedUser.getRoleId().trim() : "";
                    
                    // Heavy DB check: Role and permissions lookup
                    userRole = roleService.getRoleById(userRoleId);
                    userPermissions = (userRole != null && userRole.getPermissions() != null) 
                            ? userRole.getPermissions().trim() : "";

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

                    // Role verification
                    boolean isRoleMatch = userRoleId.equalsIgnoreCase(selectedRoleId) ||
                                          computedDbRoleName.equalsIgnoreCase(selectedRoleName) ||
                                          computedDbRoleName.replace(" ", "_").equalsIgnoreCase(selectedRoleId) ||
                                          selectedRoleName.equalsIgnoreCase(computedDbRoleName);

                    if (!isRoleMatch) {
                        AuditServiceImpl.getInstance().log("AUTH", "LOGIN_ROLE_MISMATCH",
                                "Access denied for user " + username + ": Assigned role (" + computedDbRoleName 
                                + ") does not match selected role (" + selectedRoleName + ")", "FAILED");
                        errorType = 2;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorType = 3;
            }

            final int finalStatus = errorType;
            final User finalUser = authenticatedUser;
            final String finalDbRoleName = computedDbRoleName;
            final String finalPermissions = userPermissions;

            // 4. Safely return back to the ZK UI Thread
            Executions.schedule(desktop, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) {
                    // Clear busy state and restore button
                    Clients.clearBusy();
                    btnSignIn.setDisabled(false);
                    btnSignIn.setLabel("Sign In");

                    if (finalStatus == 1) {
                        Messagebox.show("Invalid username or password.", "Authentication Failed", Messagebox.OK, Messagebox.ERROR);
                        return;
                    }

                    if (finalStatus == 2) {
                        Messagebox.show("Access Denied: Assigned role (" + finalDbRoleName +
                                        ") does not match selected role (" + selectedRoleName + ").",
                                        "Role Mismatch", Messagebox.OK, Messagebox.EXCLAMATION);
                        return;
                    }

                    if (finalStatus == 3 || finalUser == null) {
                        Messagebox.show("A system error occurred during authentication. Please try again.", "Error", Messagebox.OK, Messagebox.ERROR);
                        return;
                    }

                    // Success: Bind session attributes
                    String normalizedRole = finalDbRoleName.toUpperCase().replace(" ", "_");
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

                    session.setAttribute("LOGGED_USER", finalUser.getFullName());
                    session.setAttribute("USER_ID", finalUser.getUserId());
                    session.setAttribute("USERNAME", finalUser.getUsername());
                    session.setAttribute("CTS_USERNAME", finalUser.getUsername());
                    session.setAttribute("USER_ROLE", normalizedRole);
                    session.setAttribute("CTS_USER_ROLE", normalizedRole);
                    session.setAttribute("ROLE_ID", finalUser.getRoleId());
                    session.setAttribute("ROLE_NAME", finalDbRoleName);
                    session.setAttribute("USER_OBJ", finalUser);
                    session.setAttribute("CLEARING_DATE", sdf.format(new Date()));
                    session.setAttribute("USER_PERMISSIONS", finalPermissions);

                    // Audit success log
                    AuditServiceImpl.getInstance().log("AUTH", "LOGIN",
                            "User " + finalUser.getUsername() + " logged in successfully with role " + finalDbRoleName, "SUCCESS");

                    // Navigate user by role
                    String userRoleId = finalUser.getRoleId() != null ? finalUser.getRoleId().trim() : "";
                    if ("ROL1001".equalsIgnoreCase(userRoleId) || normalizedRole.contains("ADMIN")) {
                        Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
                    } else if ("ROL1002".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("OUTWARD"))) {
                        Executions.sendRedirect("/outward/maker/maker-dashboard.zul");
                    } else if ("ROL1003".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("OUTWARD"))) {
                        Executions.sendRedirect("/outward/checker/dashboard.zul");
                    } else if ("ROL1004".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("INWARD"))) {
                        Executions.sendRedirect("/inward/maker/index.zul");
                    } else if ("ROL1005".equalsIgnoreCase(userRoleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("INWARD"))) {
                        Executions.sendRedirect("/inward/checker/dashboard.zul");
                    } else {
                        Messagebox.show("No dashboard mapped for role: " + finalDbRoleName + " (ID: " + userRoleId + ")",
                                        "Navigation Error", Messagebox.OK, Messagebox.ERROR);
                    }
                }
            }, new Event("onLoginFinished"));
        });
    }
}
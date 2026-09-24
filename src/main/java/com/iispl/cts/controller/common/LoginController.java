package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Captcha;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.Role;
import com.iispl.cts.entity.User;
import com.iispl.cts.service.AuditService;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;
import com.iispl.cts.serviceimpl.RoleServiceImpl;
import com.iispl.cts.serviceimpl.UserServiceImpl;

public class LoginController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    
    private Textbox txtIdentifier;
    private Textbox txtPassword;
    private Button btnSignIn;
    private Button btnTogglePassword;
    

    private Vlayout vlCaptcha;
    private Captcha cptLogin;
    private Textbox txtCaptcha;
    

    private static final String ATTEMPTS_SESSION_KEY = "LOGIN_FAILED_ATTEMPTS";
    private static final int MAX_FAILED_ATTEMPTS = 3;

    // Concurrency guard against rapid keypress / double submit
    private final java.util.concurrent.atomic.AtomicBoolean isAuthenticating = 
            new java.util.concurrent.atomic.AtomicBoolean(false);

    // Inline Error Components
    private Div divErrorBox;
    private Label lblErrorMessage;

    private boolean isPasswordVisible = false;

    private final UserService userService = UserServiceImpl.getInstance();
    private final RoleService roleService = RoleServiceImpl.getInstance();
    private final AuditService auditService = AuditServiceImpl.getInstance();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        clearErrorMessage();
     // Check if the current session has already exceeded the failed attempt threshold
        Session session = Sessions.getCurrent();
        if (session != null) {
            Integer attempts = (Integer) session.getAttribute(ATTEMPTS_SESSION_KEY);
            if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
                enableCaptchaUI();
            }
        }
    }
    private void enableCaptchaUI() {
        if (vlCaptcha != null) {
            vlCaptcha.setVisible(true);
            if (cptLogin != null) {
                cptLogin.randomValue();
            }
        }
    }
    public void onRegenerateCaptcha() {
        if (cptLogin != null) {
            cptLogin.randomValue();
        }
        if (txtCaptcha != null) {
            txtCaptcha.setValue("");
        }
    }

    public void onOKIdentifier() {
        if (txtPassword != null) {
            txtPassword.setFocus(true);
        }
    }

    public void onOKPassword() {
        processLogin();
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

    public void onChanging$txtIdentifier(Event event) {
        clearErrorMessage();
    }

    public void onChanging$txtPassword(Event event) {
        clearErrorMessage();
    }

    private void showErrorMessage(String message) {
        if (lblErrorMessage != null) {
            lblErrorMessage.setValue(message);
        }
        if (divErrorBox != null) {
            divErrorBox.setVisible(true);
        }
    }

    private void clearErrorMessage() {
        if (divErrorBox != null) {
            divErrorBox.setVisible(false);
        }
        if (lblErrorMessage != null) {
            lblErrorMessage.setValue("");
        }
    }

    private void processLogin() {
    	System.out.println(">>> [DEBUG LOGIN] processLogin() triggered for identifier: " 
                + (txtIdentifier != null ? txtIdentifier.getValue() : "null"));
        if (!isAuthenticating.compareAndSet(false, true)) {
        	System.out.println(">>> [DEBUG LOGIN] Blocked by AtomicBoolean guard");
            return;
        }

        if (btnSignIn != null) {
            btnSignIn.setDisabled(true);
        }
        clearErrorMessage();

        try {
            final String identifier = (txtIdentifier != null && txtIdentifier.getValue() != null) ? txtIdentifier.getValue().trim() : "";
            final String password = (txtPassword != null && txtPassword.getValue() != null) ? txtPassword.getValue().trim() : "";

            if (identifier.isEmpty() || password.isEmpty()) {
                showErrorMessage("Please enter your username or email and password.");
                resetLoginButton();
                return;
            }

            Session session = Sessions.getCurrent();
            Integer attempts = (session != null) ? (Integer) session.getAttribute(ATTEMPTS_SESSION_KEY) : null;
            if (attempts == null) {
                attempts = 0;
            }

            
            // 1. CAPTCHA ENFORCEMENT CHECK
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                String enteredCaptcha = (txtCaptcha != null && txtCaptcha.getValue() != null) 
                                        ? txtCaptcha.getValue().trim() : "";

                if (enteredCaptcha.isEmpty()) {
                    showErrorMessage("Please enter the security verification code.");
                    resetLoginButton();
                    return;
                }

                if (cptLogin == null || !enteredCaptcha.equalsIgnoreCase(cptLogin.getValue())) {
                    onRegenerateCaptcha();
                    showErrorMessage("Invalid security code. Please try again.");
                    resetLoginButton();
                    return;
                }
            }         
            // 2. AUTHENTICATE CREDENTIALS
            User authenticatedUser = userService.authenticate(identifier, password);

            if (authenticatedUser == null) {
                attempts++;
                System.out.println(">>> [DEBUG LOGIN] Failed attempt count: " + attempts);
                if (session != null) {
                    session.setAttribute(ATTEMPTS_SESSION_KEY, attempts);
                }

                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    enableCaptchaUI();
                    showErrorMessage("Invalid credentials. Enter the security code to proceed.");
                } else {
                    int remaining = MAX_FAILED_ATTEMPTS - attempts;
                    showErrorMessage("Invalid username/email or password. (" + remaining + " attempts remaining before verification)");
                }

                auditService.log("-", identifier, "N/A", "AUTH", "LOGIN_FAILED", "Invalid login attempt (" + attempts + ") for: " + identifier, "FAILED");
                resetLoginButton();
                return;
            }
            
            // 3. SUCCESSFUL LOGIN: CLEAR COUNTER
            if (session != null) {
                session.removeAttribute(ATTEMPTS_SESSION_KEY);
            }

            // 4. Block inactive user accounts
            if ("INACTIVE".equalsIgnoreCase(authenticatedUser.getStatus())) {
                auditService.log(authenticatedUser.getUserId(), authenticatedUser.getUsername(), authenticatedUser.getRoleId(),
                        "AUTH", "LOGIN_BLOCKED", "Login denied for user " + authenticatedUser.getUsername() + ": Account inactivated by Admin.", "FAILED");
                showErrorMessage("Your account is deactivated.");
                resetLoginButton();
                return;
            }

            // 5. Fetch assigned role
            String userRoleId = authenticatedUser.getRoleId() != null ? authenticatedUser.getRoleId().trim() : "";
            Role userRole = roleService.getRoleById(userRoleId);
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
                else computedDbRoleName = "Unknown";
            }

            // 6. Bind Session Attributes
            String normalizedRole = computedDbRoleName.toUpperCase().replace(" ", "_");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

            if (session != null) {
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
            }
            
            Clients.showBusy("Authenticating...");

            auditService.log(
                    authenticatedUser.getUserId(),
                    authenticatedUser.getUsername(),
                    computedDbRoleName,
                    "AUTH",
                    "LOGIN",
                    "User " + authenticatedUser.getUsername() + " logged in successfully with role " + computedDbRoleName,
                    "SUCCESS"
            );

            // 7. Navigate to dashboard
            redirectToRoleDashboard(userRoleId, normalizedRole, computedDbRoleName);

        } catch (Exception e) {
            resetLoginButton();
            showErrorMessage("An unexpected error occurred during login. Please try again.");
            e.printStackTrace();
        }
    }

    private void resetLoginButton() {
        if (btnSignIn != null) {
            btnSignIn.setDisabled(false);
        }
        isAuthenticating.set(false);
    }
    
    //Role-Based Routing (RBAC)
    private void redirectToRoleDashboard(String roleId, String normalizedRole, String roleName) {
        if ("ROL1001".equalsIgnoreCase(roleId) || normalizedRole.contains("ADMIN")) {
            Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
        } else if ("ROL1002".equalsIgnoreCase(roleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/maker/maker-module.zul");
        } else if ("ROL1003".equalsIgnoreCase(roleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("OUTWARD"))) {
            Executions.sendRedirect("/outward/checker/dashboard.zul");
        } else if ("ROL1004".equalsIgnoreCase(roleId) || (normalizedRole.contains("MAKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/maker/index.zul");
        } else if ("ROL1005".equalsIgnoreCase(roleId) || (normalizedRole.contains("CHECKER") && normalizedRole.contains("INWARD"))) {
            Executions.sendRedirect("/inward/checker/dashboard.zul");
        } else {
            showErrorMessage("No dashboard mapped for role: " + roleName + " (ID: " + roleId + ")");
        }
    }
}
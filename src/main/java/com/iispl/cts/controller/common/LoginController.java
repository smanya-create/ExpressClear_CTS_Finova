package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.User;
import com.iispl.cts.service.UserService;
import com.iispl.cts.serviceimpl.UserServiceImpl;

public class LoginController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire
    private Textbox txtUsername;

    @Wire
    private Textbox txtPassword;

    @Wire
    private Combobox cmbRole;

    private final UserService userService = new UserServiceImpl();

    @Listen("onClick = #btnSignIn; onOK = #txtPassword")
    public void onClickSignIn() {
        String username = txtUsername.getValue() != null ? txtUsername.getValue().trim() : "";
        String password = txtPassword.getValue() != null ? txtPassword.getValue().trim() : "";
        Comboitem selectedItem = cmbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || selectedItem == null) {
            Messagebox.show("Please enter username, password, and select a role.", 
                            "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        String selectedRoleValue = selectedItem.getValue() != null ? selectedItem.getValue().toString().trim() : "";
        String selectedRoleLabel = selectedItem.getLabel() != null ? selectedItem.getLabel().trim() : "";

        // Authenticate credentials via UserService
        User user = userService.authenticate(username, password);

        if (user == null) {
            Messagebox.show("Invalid username or password.", "Authentication Failed", Messagebox.OK, Messagebox.ERROR);
            return;
        }

        String dbRole = user.getRoleId() != null ? user.getRoleId().trim() : "";

        System.out.println("[LOGIN DEBUG] DB Role: '" + dbRole + 
                           "' | Selected UI Value: '" + selectedRoleValue + 
                           "' | Selected UI Label: '" + selectedRoleLabel + "'");

        // Case-insensitive role comparison against value or label
        boolean isRoleMatch = dbRole.equalsIgnoreCase(selectedRoleValue) || 
                              dbRole.equalsIgnoreCase(selectedRoleLabel);

        if (!isRoleMatch) {
            Messagebox.show("Access Denied: Assigned role (" + dbRole + 
                            ") does not match selected role (" + selectedRoleLabel + ").", 
                            "Role Mismatch", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // Session Attributes Setup
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        Sessions.getCurrent().setAttribute("LOGGED_USER", user.getFullName());
        Sessions.getCurrent().setAttribute("USER_ID", user.getUserId());
        Sessions.getCurrent().setAttribute("USERNAME", user.getUsername());
        Sessions.getCurrent().setAttribute("USER_ROLE", dbRole);
        Sessions.getCurrent().setAttribute("USER_OBJ", user);
        Sessions.getCurrent().setAttribute("CLEARING_DATE", sdf.format(new Date()));

        // Dashboard Redirect Logic
        switch (dbRole.toUpperCase()) {
            case "ADMIN":
                Executions.sendRedirect("/admin/dashboard/admin-dashboard.zul");
                break;
            case "OUTWARD_MAKER":
                Executions.sendRedirect("/outward/maker/maker-dashboard.zul");
                break;
            case "OUTWARD_CHECKER":
                Executions.sendRedirect("/outward/checker/checker-dashboard.zul");
                break;
            case "INWARD_MAKER":
                Executions.sendRedirect("/inward/maker/index.zul");
                break;
            case "INWARD_CHECKER":
                Executions.sendRedirect("/inward/checker/inward-checker-dashboard.zul");
                break;
            default:
                Messagebox.show("No dashboard mapped for role: " + dbRole, "Navigation Error", Messagebox.OK, Messagebox.ERROR);
                break;
        }
    }
}
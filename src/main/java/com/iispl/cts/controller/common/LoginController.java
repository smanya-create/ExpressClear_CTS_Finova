package com.iispl.cts.controller.common;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

public class LoginController extends GenericForwardComposer<Component> {

    private Textbox txtUsername;
    private Textbox txtPassword;
    private Combobox cmbRole;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    public void onClickSignIn() {
        String username = txtUsername.getValue() != null ? txtUsername.getValue().trim() : "";
        String password = txtPassword.getValue() != null ? txtPassword.getValue().trim() : "";
        Comboitem selectedItem = cmbRole.getSelectedItem();

        // 1. Validate Username
        if (username.isEmpty()) {
            Messagebox.show("Username is required.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            txtUsername.focus();
            return;
        }

        // 2. Validate Password
        if (password.isEmpty()) {
            Messagebox.show("Password is required.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            txtPassword.focus();
            return;
        }

        // 3. Validate Role Selection
        if (selectedItem == null) {
            Messagebox.show("Please select a role to proceed.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            cmbRole.focus();
            return;
        }

        String roleValue = selectedItem.getValue().toString();

        // Save User Session Context to HTTP SESSION (Persists across page redirects)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDateStr = sdf.format(new Date());

        // Save dynamic session attributes
        Sessions.getCurrent().setAttribute("LOGGED_USER", username);
        Sessions.getCurrent().setAttribute("USER_ROLE", roleValue);
        Sessions.getCurrent().setAttribute("CLEARING_DATE", currentDateStr);
        
        // Role-Based Redirects
        switch (roleValue) {
            case "ADMIN":
                Executions.sendRedirect("/admin/dashboard.zul");
                break;
            case "OUTWARD_MAKER":
                Executions.sendRedirect("/outward/maker/maker-dashboard.zul");
                break;
            case "OUTWARD_CHECKER":
                Executions.sendRedirect("/outward/checker/checker-dashboard.zul");
                break;
            case "INWARD_MAKER":
                Executions.sendRedirect("/inward/maker/inward-maker-dashboard.zul");
                break;
            case "INWARD_CHECKER":
                Executions.sendRedirect("/inward/checker/inward-checker-dashboard.zul");
                break;
            default:
                Messagebox.show("Invalid role selected.", "Access Error", Messagebox.OK, Messagebox.ERROR);
                break;
        }
    }
}

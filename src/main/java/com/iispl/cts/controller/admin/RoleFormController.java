package com.iispl.cts.controller.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.serviceimpl.RoleServiceImpl;

public class RoleFormController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Form inputs
    private Textbox txtRoleId;
    private Textbox txtRoleName;
    private Textbox txtDescription;
    private Combobox cmbStatus;
    private Checkbox chkSelectAll;

    // Permission checkboxes
    private Checkbox chkDashboard;
    private Checkbox chkBatchUpload;
    private Checkbox chkBatchList;
    private Checkbox chkUnprocessedQueue;
    private Checkbox chkMicrRepair;
    private Checkbox chkDataEntry;
    private Checkbox chkChequeDetails;
    private Checkbox chkSubmitBatch;
    private Checkbox chkCheckerVerification;
    private Checkbox chkReports;
    private Checkbox chkEodBod;
    private Checkbox chkAdminUserMgmt;

    private Button btnSave;
    private Button btnCancel;

    private final RoleService roleService = RoleServiceImpl.getInstance();
    private String roleIdParam;
    private boolean isModifyMode = false;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        roleIdParam = Executions.getCurrent().getParameter("roleId");

        if (roleIdParam != null && !roleIdParam.trim().isEmpty()) {
            isModifyMode = true;
            loadRoleForEdit(roleIdParam.trim());
        } else {
            isModifyMode = false;
            // Generate and assign next sequential Role ID
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

        if (role.getPermissions() != null) {
            List<String> perms = Arrays.asList(role.getPermissions().split(","));
            if (chkDashboard != null) chkDashboard.setChecked(perms.contains("DASHBOARD"));
            if (chkBatchUpload != null) chkBatchUpload.setChecked(perms.contains("BATCH_UPLOAD"));
            if (chkBatchList != null) chkBatchList.setChecked(perms.contains("BATCH_LIST"));
            if (chkUnprocessedQueue != null) chkUnprocessedQueue.setChecked(perms.contains("UNPROCESSED_QUEUE"));
            if (chkMicrRepair != null) chkMicrRepair.setChecked(perms.contains("MICR_REPAIR"));
            if (chkDataEntry != null) chkDataEntry.setChecked(perms.contains("DATA_ENTRY"));
            if (chkChequeDetails != null) chkChequeDetails.setChecked(perms.contains("CHEQUE_DETAILS"));
            if (chkSubmitBatch != null) chkSubmitBatch.setChecked(perms.contains("SUBMIT_BATCH"));
            if (chkCheckerVerification != null) chkCheckerVerification.setChecked(perms.contains("CHECKER_VERIFICATION"));
            if (chkReports != null) chkReports.setChecked(perms.contains("REPORTS"));
            if (chkEodBod != null) chkEodBod.setChecked(perms.contains("EOD_BOD"));
            if (chkAdminUserMgmt != null) chkAdminUserMgmt.setChecked(perms.contains("USER_MANAGEMENT"));
        }
    }

    public void onCheck$chkSelectAll(CheckEvent event) {
        boolean check = event.isChecked();
        if (chkDashboard != null) chkDashboard.setChecked(check);
        if (chkBatchUpload != null) chkBatchUpload.setChecked(check);
        if (chkBatchList != null) chkBatchList.setChecked(check);
        if (chkUnprocessedQueue != null) chkUnprocessedQueue.setChecked(check);
        if (chkMicrRepair != null) chkMicrRepair.setChecked(check);
        if (chkDataEntry != null) chkDataEntry.setChecked(check);
        if (chkChequeDetails != null) chkChequeDetails.setChecked(check);
        if (chkSubmitBatch != null) chkSubmitBatch.setChecked(check);
        if (chkCheckerVerification != null) chkCheckerVerification.setChecked(check);
        if (chkReports != null) chkReports.setChecked(check);
        if (chkEodBod != null) chkEodBod.setChecked(check);
        if (chkAdminUserMgmt != null) chkAdminUserMgmt.setChecked(check);
    }

    private String buildPermissionString() {
        List<String> perms = new ArrayList<>();
        if (chkDashboard != null && chkDashboard.isChecked()) perms.add("DASHBOARD");
        if (chkBatchUpload != null && chkBatchUpload.isChecked()) perms.add("BATCH_UPLOAD");
        if (chkBatchList != null && chkBatchList.isChecked()) perms.add("BATCH_LIST");
        if (chkUnprocessedQueue != null && chkUnprocessedQueue.isChecked()) perms.add("UNPROCESSED_QUEUE");
        if (chkMicrRepair != null && chkMicrRepair.isChecked()) perms.add("MICR_REPAIR");
        if (chkDataEntry != null && chkDataEntry.isChecked()) perms.add("DATA_ENTRY");
        if (chkChequeDetails != null && chkChequeDetails.isChecked()) perms.add("CHEQUE_DETAILS");
        if (chkSubmitBatch != null && chkSubmitBatch.isChecked()) perms.add("SUBMIT_BATCH");
        if (chkCheckerVerification != null && chkCheckerVerification.isChecked()) perms.add("CHECKER_VERIFICATION");
        if (chkReports != null && chkReports.isChecked()) perms.add("REPORTS");
        if (chkEodBod != null && chkEodBod.isChecked()) perms.add("EOD_BOD");
        if (chkAdminUserMgmt != null && chkAdminUserMgmt.isChecked()) perms.add("USER_MANAGEMENT");
        return String.join(",", perms);
    }

    public void onClick$btnSave(Event event) {
        String roleId = (txtRoleId != null && txtRoleId.getValue() != null) ? txtRoleId.getValue().trim() : "";
        String roleName = (txtRoleName != null && txtRoleName.getValue() != null) ? txtRoleName.getValue().trim() : "";
        String desc = (txtDescription != null && txtDescription.getValue() != null) ? txtDescription.getValue().trim() : "";
        String status = (cmbStatus != null && cmbStatus.getSelectedItem() != null) ? cmbStatus.getSelectedItem().getLabel() : "Active";
        String perms = buildPermissionString();

        if (roleName.isEmpty()) {
            Messagebox.show("Please enter Role Name.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        Role role = new Role(roleId, roleName, desc, status, perms, new Timestamp(System.currentTimeMillis()));

        boolean success = isModifyMode ? roleService.updateRole(role) : roleService.saveRole(role);

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
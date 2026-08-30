package com.iispl.cts.controller.admin;

import java.text.SimpleDateFormat;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.serviceimpl.RoleServiceImpl;

public class RoleManagementController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Component wires (auto-wired by component ID)
    private Textbox txtSearchRole;
    private Button btnResetSearch;
    private Label lblRoleCount;
    private Rows rowsRoles;

    private final RoleService roleService = RoleServiceImpl.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        loadRoleData("");
    }

    private void loadRoleData(String filterText) {
        List<Role> allRoles = roleService.getAllRoles();

        List<Role> filteredRoles;
        if (filterText == null || filterText.trim().isEmpty()) {
            filteredRoles = allRoles;
        } else {
            String lowerFilter = filterText.trim().toLowerCase();
            filteredRoles = allRoles.stream()
                    .filter(r -> (r.getRoleId() != null && r.getRoleId().toLowerCase().contains(lowerFilter))
                            || (r.getRoleName() != null && r.getRoleName().toLowerCase().contains(lowerFilter)))
                    .toList();
        }

        if (lblRoleCount != null) {
            lblRoleCount.setValue(filteredRoles.size() + " roles found");
        }

        renderRoleRows(filteredRoles);
    }

    private void renderRoleRows(List<Role> roles) {
        if (rowsRoles == null) return;
        rowsRoles.getChildren().clear();

        for (Role role : roles) {
            Row row = new Row();

            // ROLE ID
            Label lblId = new Label(role.getRoleId());
            lblId.setStyle("color: #4a5568; font-weight: 500; font-size: 13px;");
            row.appendChild(lblId);

            // ROLE NAME
            Label lblName = new Label(role.getRoleName());
            lblName.setStyle("color: #2d3748; font-weight: 600; font-size: 13px;");
            row.appendChild(lblName);

            // CREATED DATE
            String createdDateStr = role.getRoleCreatedAt() != null 
                    ? dateFormat.format(role.getRoleCreatedAt()) 
                    : "-";
            Label lblCreated = new Label(createdDateStr);
            lblCreated.setStyle("color: #718096; font-size: 13px;");
            row.appendChild(lblCreated);

            // PERMISSIONS / DESCRIPTION BADGE
            Label lblScope = new Label(resolveRoleScope(role.getRoleName()));
            lblScope.setStyle("color: #1a3962; font-size: 12px; font-weight: 600; background: #e2e8f0; padding: 3px 8px; border-radius: 4px;");
            row.appendChild(lblScope);

            rowsRoles.appendChild(row);
        }
    }

    private String resolveRoleScope(String roleName) {
        if (roleName == null) return "Standard Access";
        String upper = roleName.toUpperCase();
        if (upper.contains("ADMIN")) return "System Administration & EOD/BOD";
        if (upper.contains("OUTWARD") && upper.contains("MAKER")) return "Outward Cheque Clearing Data Entry";
        if (upper.contains("OUTWARD") && upper.contains("CHECKER")) return "Outward Verification & XML Posting";
        if (upper.contains("INWARD") && upper.contains("MAKER")) return "Inward Intake & Repair";
        if (upper.contains("INWARD") && upper.contains("CHECKER")) return "Inward Verification & RRF Generation";
        return "Standard Portal Access";
    }

    // Auto-wired event handlers for GenericForwardComposer convention (onEvent$componentId)
    public void onChanging$txtSearchRole(InputEvent event) {
        loadRoleData(event.getValue());
    }

    public void onOK$txtSearchRole(Event event) {
        if (txtSearchRole != null) {
            loadRoleData(txtSearchRole.getValue());
        }
    }

    public void onClick$btnResetSearch(Event event) {
        if (txtSearchRole != null) {
            txtSearchRole.setValue("");
        }
        loadRoleData("");
    }
}
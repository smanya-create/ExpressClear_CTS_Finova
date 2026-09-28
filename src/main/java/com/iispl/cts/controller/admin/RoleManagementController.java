package com.iispl.cts.controller.admin;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;
import com.iispl.cts.serviceimpl.RoleServiceImpl;

public class RoleManagementController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	// UI components
	private Button btnAddRole;
	private Label lblRoleCount;
	private Rows rowsRoles;

	private final RoleService roleService = RoleServiceImpl.getInstance();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		if (!SecurityUtil.checkAccess(null)) {
			return;
		}
		super.doAfterCompose(comp);

		loadRoles();
	}

	private void loadRoles() {
		// Fetch all roles without filtering
		List<Role> roles = roleService.searchRoles("", "ALL");

		if (lblRoleCount != null) {
			lblRoleCount.setValue(roles.size() + " roles defined");
		}

		if (rowsRoles == null)
			return;

		rowsRoles.getChildren().clear();

		for (Role role : roles) {
			Row row = new Row();
			row.setStyle("border-bottom: 1px solid #f1f5f9; height: 50px;");

			// Role ID
			Label lblId = new Label(role.getRoleId());
			lblId.setStyle(
					"color: #475569; font-size: 13px; font-weight: 600; text-align: left; display: block; padding-left: 12px;");
			row.appendChild(lblId);

			// Role Name
			Label lblName = new Label(role.getRoleName());
			lblName.setStyle(
					"color: #1e293b; font-weight: 500; font-size: 13px; text-align: left; display: block; padding-left: 12px;");
			row.appendChild(lblName);

			// Description
			Label lblDesc = new Label(role.getDescription() != null ? role.getDescription() : "-");
			lblDesc.setStyle("color: #64748b; font-size: 13px; text-align: left; display: block; padding-left: 12px;");
			row.appendChild(lblDesc);

			// Status badge
			Div statusWrapper = new Div();
			statusWrapper.setStyle("text-align: center; width: 100%;");

			String status = (role.getStatus() != null && !role.getStatus().trim().isEmpty()) ? role.getStatus().trim()
					: "Active";

			Label lblStatus = new Label(status);
			if ("ACTIVE".equalsIgnoreCase(status)) {
				lblStatus.setStyle(
						"background: #dcfce7; color: #15803d; padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; display: inline-block;");
			} else {
				lblStatus.setStyle(
						"background: #fee2e2; color: #b91c1c; padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; display: inline-block;");
			}
			statusWrapper.appendChild(lblStatus);
			row.appendChild(statusWrapper);

			// Modify action button
			Div actionWrapper = new Div();
			actionWrapper.setStyle("text-align: center; width: 100%;");

			Button btnModify = new Button("Modify");
			btnModify.setIconSclass("z-icon-pencil");
			btnModify.setSclass("btn-action-modify");
			btnModify.addEventListener("onClick", e -> {
				Executions.sendRedirect("/admin/role/modify-role.zul?roleId=" + role.getRoleId());
			});
			actionWrapper.appendChild(btnModify);
			row.appendChild(actionWrapper);

			rowsRoles.appendChild(row);
		}
	}

	public void onClick$btnAddRole(Event event) {
		Executions.sendRedirect("/admin/role/add-role.zul");
	}
}
package com.iispl.cts.serviceimpl;


import com.iispl.cts.dao.RoleDAO;
import com.iispl.cts.daoimpl.RoleDAOImpl;
import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;

import java.util.List;

public class RoleServiceImpl implements RoleService {

	private static RoleServiceImpl instance;
    private final RoleDAO roleDAO = RoleDAOImpl.getInstance();

    public static synchronized RoleServiceImpl getInstance() {
        if (instance == null) instance = new RoleServiceImpl();
        return instance;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDAO.searchRoles(null, null);
    }

    @Override
    public List<Role> searchRoles(String query, String status) {
        return roleDAO.searchRoles(query, status);
    }

    @Override
    public Role getRoleById(String roleId) {
        return (roleId != null && !roleId.trim().isEmpty()) ? roleDAO.findById(roleId.trim()) : null;
    }

    @Override
    public Role getRoleByName(String roleName) {
        return (roleName != null && !roleName.trim().isEmpty()) ? roleDAO.findByName(roleName.trim()) : null;
    }

    @Override
    public boolean saveRole(Role role) {
        return roleDAO.saveRole(role);
    }

    @Override
    public boolean updateRole(Role role) {
        return roleDAO.updateRole(role);
    }

    @Override
    public String generateNextRoleId() {
        return roleDAO.generateNextRoleId();
    }
}

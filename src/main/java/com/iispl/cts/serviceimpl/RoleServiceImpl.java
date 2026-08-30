package com.iispl.cts.serviceimpl;


import com.iispl.cts.dao.RoleDAO;
import com.iispl.cts.daoimpl.RoleDAOImpl;
import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;

import java.util.List;

public class RoleServiceImpl implements RoleService {

    private static RoleServiceImpl instance;
    private final RoleDAO roleDAO = RoleDAOImpl.getInstance();

    public RoleServiceImpl() {}

    public static synchronized RoleServiceImpl getInstance() {
        if (instance == null) {
            instance = new RoleServiceImpl();
        }
        return instance;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDAO.getAllRoles();
    }

    @Override
    public Role getRoleById(String roleId) {
        if (roleId == null || roleId.trim().isEmpty()) {
            return null;
        }
        return roleDAO.findById(roleId.trim());
    }

    @Override
    public Role getRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return null;
        }
        return roleDAO.findByName(roleName.trim());
    }
}

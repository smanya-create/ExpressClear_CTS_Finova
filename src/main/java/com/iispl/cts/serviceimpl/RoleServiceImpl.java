package com.iispl.cts.serviceimpl;

import com.iispl.cts.dao.RoleDAO;
import com.iispl.cts.daoimpl.RoleDAOImpl;
import com.iispl.cts.entity.Role;
import com.iispl.cts.service.RoleService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoleServiceImpl implements RoleService {

    private static RoleServiceImpl instance;
    private final RoleDAO roleDAO = RoleDAOImpl.getInstance();

    // In-memory cache for fast lookup during user login and session initialization
    private static final Map<String, Role> ROLE_ID_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Role> ROLE_NAME_CACHE = new ConcurrentHashMap<>();

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
        if (roleId == null || roleId.trim().isEmpty()) {
            return null;
        }
        String cleanId = roleId.trim();
        // Return from cache or fetch from DB and store
        return ROLE_ID_CACHE.computeIfAbsent(cleanId, id -> roleDAO.findById(id));
    }

    @Override
    public Role getRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return null;
        }
        String cleanName = roleName.trim();
        return ROLE_NAME_CACHE.computeIfAbsent(cleanName, name -> roleDAO.findByName(name));
    }

    @Override
    public boolean saveRole(Role role) {
        boolean success = roleDAO.saveRole(role);
        if (success) {
            clearCache();
        }
        return success;
    }

    @Override
    public boolean updateRole(Role role) {
        boolean success = roleDAO.updateRole(role);
        if (success) {
            clearCache();
        }
        return success;
    }

    @Override
    public String generateNextRoleId() {
        return roleDAO.generateNextRoleId();
    }

    @Override
    public boolean isRoleNameExists(String roleName) {
        return roleDAO.isRoleNameExists(roleName);
    }

    @Override
    public boolean isRoleNameExists(String roleName, String excludeRoleId) {
        return roleDAO.isRoleNameExists(roleName, excludeRoleId);
    }

    // Clear cache when roles are modified
    public static void clearCache() {
        ROLE_ID_CACHE.clear();
        ROLE_NAME_CACHE.clear();
    }
}
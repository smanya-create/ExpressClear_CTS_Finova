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

    // Cache role entities in memory by id and name
    private static final Map<String, Role> ROLE_ID_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Role> ROLE_NAME_CACHE = new ConcurrentHashMap<>();

    // Singleton instance access
    public static synchronized RoleServiceImpl getInstance() {
        if (instance == null) instance = new RoleServiceImpl();
        return instance;
    }

    // Fetches all roles without applying filters
    @Override
    public List<Role> getAllRoles() {
        return roleDAO.searchRoles(null, null);
    }

    // Searches roles by keyword and status filter
    @Override
    public List<Role> searchRoles(String query, String status) {
        return roleDAO.searchRoles(query, status);
    }

    // Gets role by ID with cache-aside lookup
    @Override
    public Role getRoleById(String roleId) {
        if (roleId == null || roleId.trim().isEmpty()) {
            return null;
        }
        String cleanId = roleId.trim();
        return ROLE_ID_CACHE.computeIfAbsent(cleanId, id -> roleDAO.findById(id));
    }

    // Gets role by name with cache-aside lookup
    @Override
    public Role getRoleByName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return null;
        }
        String cleanName = roleName.trim();
        return ROLE_NAME_CACHE.computeIfAbsent(cleanName, name -> roleDAO.findByName(name));
    }

    // Persists new role and flushes cache
    @Override
    public boolean saveRole(Role role) {
        boolean success = roleDAO.saveRole(role);
        if (success) {
            clearCache();
        }
        return success;
    }

    // Updates role details and flushes cache
    @Override
    public boolean updateRole(Role role) {
        boolean success = roleDAO.updateRole(role);
        if (success) {
            clearCache();
        }
        return success;
    }

    // Generates the next sequential role identifier
    @Override
    public String generateNextRoleId() {
        return roleDAO.generateNextRoleId();
    }

    // Validates if role name already exists
    @Override
    public boolean isRoleNameExists(String roleName) {
        return roleDAO.isRoleNameExists(roleName);
    }

    // Validates if role name already exists excluding current role ID
    @Override
    public boolean isRoleNameExists(String roleName, String excludeRoleId) {
        return roleDAO.isRoleNameExists(roleName, excludeRoleId);
    }

    // Invalidates all cached roles on mutation
    public static void clearCache() {
        ROLE_ID_CACHE.clear();
        ROLE_NAME_CACHE.clear();
    }
}
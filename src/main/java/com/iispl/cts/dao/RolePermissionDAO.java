package com.iispl.cts.dao;

import java.util.Set;

public interface RolePermissionDAO {
	// Get all screen IDs assigned to a role
    Set<String> getAssignedScreenIdsByRole(String roleId);

    // Save or update screen IDs for a role (within a transaction)
    void saveRolePermissions(String roleId, Set<String> screenIds) throws Exception;

    // Get screen codes for session validation upon user login
    Set<String> getPermittedScreenCodesByRole(String roleId);

}

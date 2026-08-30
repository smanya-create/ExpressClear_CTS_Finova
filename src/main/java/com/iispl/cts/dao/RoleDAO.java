package com.iispl.cts.dao;


import java.util.List;
import com.iispl.cts.entity.Role;

public interface RoleDAO {

    /**
     * Retrieves all roles from the database ordered by role_id.
     *
     * @return List of Role entities
     */
    List<Role> getAllRoles();

    /**
     * Retrieves a role record by its unique ID (e.g., ROL1001).
     *
     * @param roleId The unique role ID
     * @return Role entity if found, null otherwise
     */
    Role findById(String roleId);

    /**
     * Retrieves a role record by its display/system name.
     *
     * @param roleName The role name (case-insensitive)
     * @return Role entity if found, null otherwise
     */
    Role findByName(String roleName);
}

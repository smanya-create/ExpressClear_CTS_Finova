package com.iispl.cts.service;


import java.util.List;
import com.iispl.cts.entity.Role;

public interface RoleService {

    /**
     * Fetches all registered roles.
     *
     * @return List of Role objects
     */
    List<Role> getAllRoles();

    /**
     * Looks up a role by its primary key (role_id).
     *
     * @param roleId Unique ID of the role
     * @return Role object if found, null otherwise
     */
    Role getRoleById(String roleId);

    /**
     * Looks up a role by name (e.g., "Admin", "Maker Outward").
     *
     * @param roleName Name of the role
     * @return Role object if found, null otherwise
     */
    Role getRoleByName(String roleName);
}
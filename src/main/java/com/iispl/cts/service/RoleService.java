package com.iispl.cts.service;


import java.util.List;
import com.iispl.cts.entity.Role;

public interface RoleService {

	List<Role> getAllRoles();
    List<Role> searchRoles(String query, String status);
    Role getRoleById(String roleId);
    Role getRoleByName(String roleName);
    boolean saveRole(Role role);
    boolean updateRole(Role role);
    String generateNextRoleId();
}
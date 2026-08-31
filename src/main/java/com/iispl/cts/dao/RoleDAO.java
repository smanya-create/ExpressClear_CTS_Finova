package com.iispl.cts.dao;


import java.util.List;
import com.iispl.cts.entity.Role;

public interface RoleDAO {

	List<Role> searchRoles(String query, String status);
    Role findById(String roleId);
    Role findByName(String roleName);
    boolean saveRole(Role role);
    boolean updateRole(Role role);
    String generateNextRoleId();
}

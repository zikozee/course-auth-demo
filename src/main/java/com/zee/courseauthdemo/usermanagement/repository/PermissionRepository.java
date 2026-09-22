package com.zee.courseauthdemo.usermanagement.repository;


import com.zee.courseauthdemo.usermanagement.entity.Permission;
import com.zee.courseauthdemo.usermanagement.permission.PermissionAccessLevel;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Repository
public interface PermissionRepository extends ListCrudRepository<Permission, Long> {
    boolean existsByPermissionName(String permissionName);
    List<Permission> findPermissionByPermissionAccessLevel(PermissionAccessLevel accessLevel);
}

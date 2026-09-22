package com.zee.courseauthdemo.usermanagement.repository;


import com.zee.courseauthdemo.usermanagement.entity.RolePermission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Repository
public interface RolePermissionRepository extends CrudRepository<RolePermission, Long> {

    boolean existsByRoleIdAndPermissionId(long roleId, long permissionId);
}
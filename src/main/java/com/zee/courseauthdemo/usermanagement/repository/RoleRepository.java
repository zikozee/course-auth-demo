package com.zee.courseauthdemo.usermanagement.repository;

import com.zee.courseauthdemo.usermanagement.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findRoleByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
}
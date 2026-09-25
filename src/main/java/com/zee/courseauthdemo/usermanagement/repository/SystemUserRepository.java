package com.zee.courseauthdemo.usermanagement.repository;

import com.zee.courseauthdemo.usermanagement.entity.SystemUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Repository
public interface SystemUserRepository extends CrudRepository<SystemUser, Long> {

   boolean existsByUsernameAndRole(String username, String role);

   Optional<SystemUser> findByUsername(String username);

   @Query(nativeQuery = true, value = """
        SELECT p.NAME FROM PERMISSION p WHERE p.ID IN (SELECT rp.PERM_ID
        FROM ROLE_PERMISSION rp
        INNER JOIN ROLE r ON rp.ROLE_ID = r.ID WHERE r.NAME =?1)
        """)
   List<String> getPermissionsByRoleName(String role);

   @Query(nativeQuery = true, value = """
    SELECT p.NAME FROM users u
    INNER JOIN ROLE r
        ON r.NAME = u.ROLE
    INNER JOIN ROLE_PERMISSION rp
        ON rp.ROLE_ID = r.ID
    INNER JOIN PERMISSION p
        ON p.ID = rp.PERM_ID
    WHERE u.USERNAME = :usernameOrEmail
       OR u.EMAIL = :usernameOrEmail
    """)
   List<String> getPermissionsByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
}
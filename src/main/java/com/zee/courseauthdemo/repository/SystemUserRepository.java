package com.zee.courseauthdemo.repository;


import com.zee.courseauthdemo.entity.SystemUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @dev : Ezekiel Eromosei
 * @date : 05 Jun, 2026
 */

@Repository
public interface SystemUserRepository extends CrudRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(String username);
}

package com.zee.courseauthdemo.repository;

import com.zee.courseauthdemo.entity.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author : Ezekiel Eromosei
 * @code @created : 17 Dec, 2024
 */

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
}

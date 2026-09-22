package com.zee.courseauthdemo.usermanagement.service;

import com.zee.courseauthdemo.usermanagement.entity.Permission;
import com.zee.courseauthdemo.usermanagement.permission.CreatePermissionDto;
import com.zee.courseauthdemo.usermanagement.permission.PermissionAccessLevel;
import com.zee.courseauthdemo.usermanagement.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository repository;

    public void save(CreatePermissionDto permissionDTO) {

        Permission permission = new Permission();
        permission.setPermissionName(permissionDTO.permission());
        permission.setDescription(permissionDTO.description());
        permission.setPermissionAccessLevel(permissionDTO.permissionAccessLevel());

        repository.save(permission);
    }

    public boolean existsByPermissionName(String permissionName) {
        return repository.existsByPermissionName(permissionName);
    }

    public List<Permission> findAll() {
        return repository.findAll();
    }

    public List<Permission> findByPermissionAccessLevel(PermissionAccessLevel permissionAccessLevel) {
        return repository.findPermissionByPermissionAccessLevel(permissionAccessLevel);
    }
}

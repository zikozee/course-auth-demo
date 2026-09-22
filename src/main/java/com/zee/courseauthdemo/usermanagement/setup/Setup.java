package com.zee.courseauthdemo.usermanagement.setup;


import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.usermanagement.entity.Permission;
import com.zee.courseauthdemo.usermanagement.entity.Role;
import com.zee.courseauthdemo.usermanagement.entity.RolePermission;
import com.zee.courseauthdemo.usermanagement.exception.UserServiceException;
import com.zee.courseauthdemo.usermanagement.permission.PermissionAccessLevel;
import com.zee.courseauthdemo.usermanagement.permission.PermissionProvider;
import com.zee.courseauthdemo.usermanagement.repository.RolePermissionRepository;
import com.zee.courseauthdemo.usermanagement.repository.RoleRepository;
import com.zee.courseauthdemo.usermanagement.repository.SystemUserRepository;
import com.zee.courseauthdemo.usermanagement.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Order(1)
@Slf4j
@Component
@RequiredArgsConstructor
public class Setup implements CommandLineRunner {
    public static final String DEFAULT_SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    // what should happen is, there should be a UI where ADMINS define some set of user roles, attached to some specific permissions
    // better still explore groups
    public static final String DEFAULT_USER_ROLE = "REGULAR_USER";


    private Integer permissionCount = 0;

    private final List<PermissionProvider> permissionProviders;
    private final PermissionService permissionService;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SystemUserRepository systemUserRepository;

    @Value("${load-permissions}")
    private boolean loadPermissions;

    @Override
    public void run(String... args) throws Exception {

        if(!loadPermissions) {
            log.info("Permissions not loaded");
            return;
        }

        log.info("Loading permissions ...");
        loadPermissions();
        log.info("Done Loading '{}' permissions.", permissionCount);

        log.info("Creating super admin default role ...");
        Role superAdminRole = createRole(DEFAULT_SUPER_ADMIN_ROLE, "SUPER ADMIN");

        log.info("Creating regular default role ...");
        Role regularRole = createRole(DEFAULT_USER_ROLE, "REGULAR USER");


        log.info("Assigning permissions to admin role ...");
        assignPermissionsToRole(superAdminRole, permissionService.findAll());
        log.info("Assigning permissions to user role ...");
        assignPermissionsToRole(regularRole, permissionService.findByPermissionAccessLevel(PermissionAccessLevel.USER));


        //todo set username User to be admin by updating the role
    }

    private void loadPermissions(){
        permissionProviders.forEach(permissionProvider ->
                permissionProvider.provide()
                        .forEach(permissionDto -> {
                            try {
                                if(!permissionService.existsByPermissionName(permissionDto.permission())){
                                    permissionService.save(permissionDto);
                                    permissionCount++;
                                }
                            }catch (Exception ex){
                                log.error("Error loading permission {}", ex.getMessage());
                                throw new UserServiceException(MessageType.ERROR, ErrorCodeConstants.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                        })
                );
    }



    private Role createRole(String roleName, String description) {
        Optional<Role> optionalRole = roleRepository.findRoleByRoleName(roleName);
        if(optionalRole.isPresent()) {
            return optionalRole.get();
        }
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        return roleRepository.save(role);
    }


    private void assignPermissionsToRole(Role role, List<Permission> permissions) {
        permissions.forEach(permission -> {
            if(!rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(role.getId());
                rp.setPermissionId(permission.getId());
                rolePermissionRepository.save(rp);
            }
        });
    }
}

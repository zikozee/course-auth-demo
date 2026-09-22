package com.zee.courseauthdemo.usermanagement.permission.system;


import com.zee.courseauthdemo.usermanagement.permission.PermissionAccessLevel;
import com.zee.courseauthdemo.usermanagement.permission.PermissionSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @dev : Ezekiel Eromosei
 * @date : 21 Sep, 2026
 */

@Getter
@AllArgsConstructor
public enum SystemPermissionSource implements PermissionSource {

    SYSTEM_ACCESS(SystemPermission.SYSTEM_ACCESS, "system to system access", PermissionAccessLevel.SYSTEM);

    private final String permission;
    private final String description;
    private final PermissionAccessLevel permissionAccessLevel;
}

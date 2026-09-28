package com.zee.courseauthdemo.usermanagement.permission.user;


import com.zee.courseauthdemo.usermanagement.permission.PermissionAccessLevel;
import com.zee.courseauthdemo.usermanagement.permission.PermissionSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @dev : Ezekiel Eromosei
 * @date : 28 Sep, 2026
 */

@Getter
@AllArgsConstructor
public enum UserPermissionSource implements PermissionSource {

    CHANGE_PASSWORD(UserPermission.CHANGE_PASSWORD, "Change Password", PermissionAccessLevel.USER);

    private final String permission;
    private final String description;
    private final PermissionAccessLevel permissionAccessLevel;
}

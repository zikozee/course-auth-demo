package com.zee.courseauthdemo.usermanagement.permission.reporting;


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
public enum ReportingPermissionSource implements PermissionSource {

    INITIATE(ReportingPermission.VIEW_PAYMENT, "View payments", PermissionAccessLevel.USER),
    STATUS(ReportingPermission.VIEW_USERS, "View Users", PermissionAccessLevel.USER);

    private final String permission;
    private final String description;
    private final PermissionAccessLevel permissionAccessLevel;
}

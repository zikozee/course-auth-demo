package com.zee.courseauthdemo.usermanagement.permission.payment;


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
public enum PaymentPermissionSource implements PermissionSource {

    INITIATE(PaymentPermission.INITIATE, "Initiate payment", PermissionAccessLevel.USER),
    STATUS(PaymentPermission.STATUS, "Get payment status", PermissionAccessLevel.USER);

    private final String permission;
    private final String description;
    private final PermissionAccessLevel permissionAccessLevel;
}

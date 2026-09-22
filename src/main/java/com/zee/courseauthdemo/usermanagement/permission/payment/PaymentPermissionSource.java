package com.zee.courseauthdemo.usermanagement.permission.payment;


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

    INITIATE(PaymentPermission.INITIATE, "Initiate payment"),
    STATUS(PaymentPermission.STATUS, "Get payment status");

    private final String permission;
    private final String description;
}

package com.zee.courseauthdemo.usermanagement.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Getter
@AllArgsConstructor
public enum PermissionAccessLevel {
    USER("DEFAULT USER ACCESS LEVEL"),
    ADMIN("DEFAULT SUPER ADMIN USER ACCESS LEVEL"),
    SYSTEM("SYSTEM ACCESS LEVEL");

    private final String description;
}

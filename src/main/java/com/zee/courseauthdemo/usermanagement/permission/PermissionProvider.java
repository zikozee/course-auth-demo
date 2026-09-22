package com.zee.courseauthdemo.usermanagement.permission;


import java.util.Set;

/**
 * @dev : Ezekiel Eromosei
 * @date : 21 Sep, 2026
 */

public interface PermissionProvider {
    Set<CreatePermissionDto> provide();
}

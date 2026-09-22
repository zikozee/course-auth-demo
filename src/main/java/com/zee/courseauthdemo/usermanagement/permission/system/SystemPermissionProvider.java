package com.zee.courseauthdemo.usermanagement.permission.system;


import com.zee.courseauthdemo.usermanagement.permission.CreatePermissionDto;
import com.zee.courseauthdemo.usermanagement.permission.PermissionProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @dev : Ezekiel Eromosei
 * @date : 21 Sep, 2026
 */

@Component
public class SystemPermissionProvider implements PermissionProvider {

    @Override
    public Set<CreatePermissionDto> provide() {
        return Arrays.stream(SystemPermissionSource.values())
                .map(permission ->
                        new CreatePermissionDto(
                                permission.getPermission(),
                                permission.getDescription()
                        )
                )
                .collect(Collectors.toSet());
    }
}

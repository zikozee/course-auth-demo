package com.zee.courseauthdemo.usermanagement.permission.payment;


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
public class PaymentPermissionProvider implements PermissionProvider {

    @Override
    public Set<CreatePermissionDto> provide() {
        return Arrays.stream(PaymentPermissionSource.values())
                .map(permission ->
                        new CreatePermissionDto(
                                permission.getPermission(),
                                permission.getDescription(),
                                permission.getPermissionAccessLevel()
                        )
                )
                .collect(Collectors.toSet());
    }
}

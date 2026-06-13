package com.zee.courseauthdemo.util;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @dev : Ezekiel Eromosei
 * @date : 18 Jan, 2026
 */

@Component
public class ProfileHolder implements EnvironmentAware {

    private static Environment environment;

    @Override
    public void setEnvironment(@NotNull Environment env) {
        ProfileHolder.environment = env;
    }

    public static boolean isProdActive() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod"));
    }
}

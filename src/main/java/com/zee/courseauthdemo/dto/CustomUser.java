package com.zee.courseauthdemo.dto;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;


/**
 * @author : Ezekiel Eromosei
 * @code @created : 01 Nov, 2023
 */

@Getter
public class CustomUser extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String role;
    private final String fullName;
    private final String email;

    public CustomUser(Long id, String fullName, String role, String email, String username, @Nullable String password,
                      boolean enabled, boolean locked, Collection<? extends GrantedAuthority> authorities) {

        super(username, password, enabled, true, true, !locked, authorities);

        this.id = id;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
    }

}
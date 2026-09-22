package com.zee.courseauthdemo.service;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.CustomUser;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.usermanagement.entity.SystemUser;
import com.zee.courseauthdemo.usermanagement.repository.SystemUserRepository;
import com.zee.courseauthdemo.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {

        SystemUser systemUser = userService.findByUsername(username);

        return new CustomUser(
                systemUser.getId(),
                systemUser.getFullName(),
                systemUser.getRole(),
                systemUser.getEmail(),
                systemUser.getUsername(),
                systemUser.getHashPassword(),
                systemUser.isEnabled(),
                systemUser.isLocked(),
                Collections.emptyList()
        );
    }
}

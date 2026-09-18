package com.zee.courseauthdemo.service;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.CustomUser;
import com.zee.courseauthdemo.entity.SystemUser;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final SystemUserRepository userRepository;

    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {

        SystemUser systemUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomOAuth2AuthenticationException(
                        new CustomOAuth2Error(ErrorCodeConstants.INCORRECT_USERNAME_PASSWORD, "username or password is incorrect", null, HttpStatus.BAD_REQUEST)
                ));

        return new CustomUser(
                systemUser.getId(),
                systemUser.getFullName(),
                systemUser.getRole(),
                systemUser.getEmail(),
                systemUser.getUsername(),
                systemUser.getHashPassword(),
                systemUser.isEnabled(),
                systemUser.isLocked(),
                Stream.of("coder") // todo get all permissions by role and replace
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
    }
}

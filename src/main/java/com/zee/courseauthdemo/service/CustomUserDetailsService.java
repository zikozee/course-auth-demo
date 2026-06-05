package com.zee.courseauthdemo.service;


import com.zee.courseauthdemo.dto.CustomUser;
import com.zee.courseauthdemo.entity.SystemUser;
import com.zee.courseauthdemo.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final SystemUserRepository userRepository;

    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {

        SystemUser user1 = new SystemUser();
        user1.setId(1L);
        user1.setUsername("user");
        user1.setHashPassword(passwordEncoder.encode("password"));
        user1.setRole("Regular");
        user1.setEnabled(true);
        user1.setLocked(false);
        user1.setFullName("Zee Zee");
        user1.setEmail("test@test.com");


        List<SystemUser> users = List.of(user1);

        // todo revert once user is now fetched fromDB
//        SystemUser systemUser = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        SystemUser systemUser = users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));


        if(systemUser.isLocked()){
            throw new LockedException("Username is locked"); // todo replace with Custom Exception
        }



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


//        //todo inject users here from a users service
//
//        return User.builder()
//                .username("user")
//                .password(passwordEncoder.encode("password"))
//                .authorities("coder")
//                .build();
    }
}

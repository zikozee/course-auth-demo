package com.zee.courseauthdemo.usermanagement.controller;


import com.zee.courseauthdemo.usermanagement.dto.ChangePasswordDto;
import com.zee.courseauthdemo.usermanagement.permission.user.UserPermission;
import com.zee.courseauthdemo.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @dev : Ezekiel Eromosei
 * @date : 28 Sep, 2026
 */

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user")
public class UserController {

    private final UserService userService;

    @PreAuthorize(value = "hasAuthority('" + UserPermission.CHANGE_PASSWORD + "')")
    @PostMapping(path = "change-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto, JwtAuthenticationToken authenticationToken){
        userService.changePassword(changePasswordDto, authenticationToken);
        return new ResponseEntity<>(Map.of("message", "password changed successfully"), HttpStatus.OK);
    }
}

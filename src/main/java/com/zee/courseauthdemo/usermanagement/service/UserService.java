package com.zee.courseauthdemo.usermanagement.service;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.usermanagement.dto.ChangePasswordDto;
import com.zee.courseauthdemo.usermanagement.entity.SystemUser;
import com.zee.courseauthdemo.usermanagement.exception.UserServiceException;
import com.zee.courseauthdemo.usermanagement.repository.SystemUserRepository;
import com.zee.courseauthdemo.util.AuthUtil;
import com.zee.courseauthdemo.util.CacheUtil;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    public static final String SYSTEM_USER_CACHE_KEY = "SYSTEM_USER_CACHE_KEY_";
    public static final String ROLE_PERMISSIONS_CACHE_KEY = "ROLE_PERMISSIONS_CACHE_KEY_";
    public static final String SYSTEM_USER_EMAIL_CACHE_KEY = "SYSTEM_USER_EMAIL_CACHE_KEY_";

    private final SystemUserRepository systemUserRepository;
    private final CacheUtil cacheUtil;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;


    public SystemUser findByUsername(String username) {
        SystemUser cachedSystemUser = cacheUtil.getData(SYSTEM_USER_CACHE_KEY + username, SystemUser.class);
        if(cachedSystemUser != null){
            return cachedSystemUser;
        }

        SystemUser systemUser = systemUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomOAuth2AuthenticationException(
                        new CustomOAuth2Error(ErrorCodeConstants.INCORRECT_USERNAME_PASSWORD, "username or password is incorrect", null, HttpStatus.BAD_REQUEST)
                ));

        cacheUtil.setGenericData(SYSTEM_USER_CACHE_KEY + username, systemUser, false, 24, TimeUnit.HOURS);
        return systemUser;
    }

    public List<String> getUserPermissionsByRole(String role){
        List<String> cachedPermissions = cacheUtil.getDataList(ROLE_PERMISSIONS_CACHE_KEY + role, String.class);
        if(!cachedPermissions.isEmpty()){
            return cachedPermissions;
        }
        List<String> permissions = systemUserRepository.getPermissionsByRoleName(role);

        cacheUtil.setGenericData(ROLE_PERMISSIONS_CACHE_KEY + role, permissions, false, 24, TimeUnit.HOURS);

        return permissions;
    }

    public List<String> getPermissionsByUsernameOrEmail(String username) {
        List<String> cachedPermissions = cacheUtil.getDataList(SYSTEM_USER_EMAIL_CACHE_KEY + username, String.class);
        if(!cachedPermissions.isEmpty()){
            return cachedPermissions;
        }

        List<String> permissions = systemUserRepository.getPermissionsByUsernameOrEmail(username);
        cacheUtil.setGenericData(SYSTEM_USER_EMAIL_CACHE_KEY + username, permissions, false, 24, TimeUnit.HOURS);
        return permissions;
    }


    // exception is different
    public SystemUser findByUsernameForUserManagement(String username) {
        SystemUser cachedSystemUser = cacheUtil.getData(SYSTEM_USER_CACHE_KEY + username, SystemUser.class);
        if(cachedSystemUser != null){
            return cachedSystemUser;
        }

        SystemUser systemUser = systemUserRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UserServiceException(MessageType.ERROR, ErrorCodeConstants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST));

        cacheUtil.setGenericData(SYSTEM_USER_CACHE_KEY + username, systemUser, false, 24, TimeUnit.HOURS);
        return systemUser;
    }


    // encrypt your password with a public key
    public void changePassword(ChangePasswordDto changePasswordDto, JwtAuthenticationToken authenticationToken){
        if(changePasswordDto.oldPassword().equals(changePasswordDto.newPassword())){
            throw new UserServiceException(MessageType.ERROR, ErrorCodeConstants.OLD_NEW_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        String username = AuthUtil.getUserNameFromJwtAuthenticationToken(authenticationToken);
        if(username == null){
            throw new UserServiceException(MessageType.ERROR, ErrorCodeConstants.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        }

        SystemUser systemUser = this.findByUsernameForUserManagement(username);
        boolean isMatch = passwordEncoder.matches(changePasswordDto.oldPassword().trim(), systemUser.getHashPassword());

        if(!isMatch){
            throw new UserServiceException(MessageType.ERROR, ErrorCodeConstants.CREDENTIALS, HttpStatus.BAD_REQUEST);
        }else {
            cacheUtil.removeKey(SYSTEM_USER_CACHE_KEY + username);
            String encode = passwordEncoder.encode(changePasswordDto.newPassword().trim());
            systemUser.setHashPassword(encode);
            SystemUser updatedUser = systemUserRepository.save(systemUser);
            log.info("username: {} password changed successfully", updatedUser.getUsername());
        }
    }
}

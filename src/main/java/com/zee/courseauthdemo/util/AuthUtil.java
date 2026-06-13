package com.zee.courseauthdemo.util;

import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.dto.ErrorMessage;
import com.zee.courseauthdemo.exception.ErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.UUID;

/**
 * @dev : Ezekiel Eromosei
 * @date : 07 Mar, 2025
 */


@Slf4j
public final class AuthUtil {

    private AuthUtil() {
        throw new OAuth2AuthenticationException("This is a utility class and cannot be instantiated");
    }

    public static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
    }

    public static UUID fromString(String uuid) {
        return UUID.fromString(uuid);
    }

    public static String uuidToString(UUID uuid) {
        return uuid.toString();
    }

    public static ApiResponse<?> parseExceptionInfoToResponseDto(MessageSourceUtil messageSourceUtil, ErrorInfo errorInfo) {
        ErrorMessage em;
        String newDetailedMessage = errorInfo.getDetailedDescription();
        try {
            em = messageSourceUtil.getErrorCodeMessage(errorInfo.getErrorCode(), errorInfo.getPlaceholderValues());
        } catch (Exception e) {
            log.debug("Failed to parse exception detail: {}, {}", errorInfo, e.getMessage(), e);
            newDetailedMessage += ", Second thrown exception is: " + e.getMessage();
            em = messageSourceUtil.getErrorCodeMessage(ErrorCodeConstants.SOMETHING_WENT_WRONG, (String) null);
        }
        return ApiResponse.<String>builder()
                .successful(false)
                .messageType(errorInfo.getMessageType())
                .errorDetailedDescription(newDetailedMessage)
                .errorCode(em.getErrorCode())
                .errorMessage(em.getMessage())
                .build();
    }
}
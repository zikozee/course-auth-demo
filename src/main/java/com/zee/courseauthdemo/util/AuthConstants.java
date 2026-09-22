package com.zee.courseauthdemo.util;


import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

/**
 * @dev : Ezekiel Eromosei
 * @date : 17 Sep, 2026
 */

public final class AuthConstants {
    private AuthConstants() {
        /* This utility class should not be instantiated */
    }

    public static final String CUSTOM_GRANT_TYPE = "custom_oauth_grant";

    public static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    public static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);
    public static final String INVALID_DPOP_PROOF = "invalid_dpop_proof";

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String EXPIRES_IN = "expiresIn";
    public static final String TOKEN_TYPE = "tokenType";

    public static final String AUTHORITIES = "authorities";
    public static final String IS_SYSTEM_TO_SYSTEM = "isSystemToSystem";

    public static final String USER_SESSION_ID = "userSessionId";
    public static final String USER_ID = "userId";
    public static final String SESSION_ID = "sessionId";
    public static final String LOGIN_SESSION_CACHE_KEY = "LOGIN_SESSION_CACHE_KEY_";
}

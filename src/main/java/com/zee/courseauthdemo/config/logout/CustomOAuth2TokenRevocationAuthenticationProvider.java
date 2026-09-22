package com.zee.courseauthdemo.config.logout;


import com.zee.courseauthdemo.config.customgrant.CustomOAuth2AuthenticationProviderUtils;
import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.UserCacheDto;
import com.zee.courseauthdemo.entity.Authorization;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.repository.impl.JpaAuthorizationService;
import com.zee.courseauthdemo.util.AuthConstants;
import com.zee.courseauthdemo.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

import java.awt.*;
import java.util.Optional;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 *  * implemented from {@link org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationProvider}
 */

@Slf4j
public class CustomOAuth2TokenRevocationAuthenticationProvider implements AuthenticationProvider {
    private final JpaAuthorizationService authorizationService;
    private final CacheUtil cacheUtil;

    public CustomOAuth2TokenRevocationAuthenticationProvider(JpaAuthorizationService authorizationService, CacheUtil cacheUtil) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(cacheUtil, "cacheUtil cannot be null");

        this.authorizationService = authorizationService;
        this.cacheUtil = cacheUtil;
    }

    @Override
    public @Nullable Authentication authenticate(@NotNull Authentication authentication) throws AuthenticationException {
        OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthentication = (OAuth2TokenRevocationAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = CustomOAuth2AuthenticationProviderUtils
                .getAuthenticatedClientElseThrowInvalidClient(tokenRevocationAuthentication);

        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        Assert.notNull(registeredClient, "registeredClient cannot be null");

        Optional<Authorization> optionalAuthorization = this.authorizationService
                .findByAccessToken(tokenRevocationAuthentication.getToken());

        if (optionalAuthorization.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("Did not authenticate token revocation request since token was not found");
            }
            // Return the authentication request when token not found
            return tokenRevocationAuthentication;
        }
        Authorization authorization = optionalAuthorization.get();
        if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
            log.info("Registered client-id doesn't match token client-id");
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, "", null, HttpStatus.BAD_REQUEST));
        }

        OAuth2Authorization oAuth2Authorization = this.authorizationService.fromAuthorizationEntity(authorization);
        OAuth2Authorization.Token<OAuth2Token> token = oAuth2Authorization.getToken(tokenRevocationAuthentication.getToken());
        Assert.notNull(token, "token cannot be null");
        authorizationService.deleteAuthRecordOnLogout(tokenRevocationAuthentication.getToken());

        final String sessionId = authorization.getSessionId();

        UserCacheDto loginCache = cacheUtil.getData(AuthConstants.LOGIN_SESSION_CACHE_KEY + sessionId, UserCacheDto.class);
        if(loginCache != null){
            cacheUtil.removeKey(AuthConstants.LOGIN_SESSION_CACHE_KEY + sessionId);
            log.info("cache cleared for session with id: {}", sessionId);
        }

        if (log.isTraceEnabled()) {
            log.trace("Saved authorization with revoked token");
            // This log is kept separate for consistency with other providers
            log.trace("Authenticated token revocation request");
        }

        return new OAuth2TokenRevocationAuthenticationToken(token.getToken(), clientPrincipal);
    }

    @Override
    public boolean supports(@NotNull Class<?> authentication) {
        return OAuth2TokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

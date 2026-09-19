package com.zee.courseauthdemo.config.refreshtoken;


import com.nimbusds.jose.jwk.JWK;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_GRANT;

/**
 * @dev : Ezekiel Eromosei
 * @date : 19 Sep, 2026
 */

@Slf4j
public class CustomRefreshTokenAuthenticationProvider implements AuthenticationProvider {

    private final JpaAuthorizationService authorizationService;
    private final CacheUtil cacheUtil;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public CustomRefreshTokenAuthenticationProvider(JpaAuthorizationService authorizationService, CacheUtil cacheUtil, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {

        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(cacheUtil, "cacheUtil cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");

        this.authorizationService = authorizationService;
        this.cacheUtil = cacheUtil;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public @Nullable Authentication authenticate(@NotNull Authentication authentication) throws AuthenticationException {
        OAuth2RefreshTokenAuthenticationToken refreshTokenAuthentication = (OAuth2RefreshTokenAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = CustomOAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(refreshTokenAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_CLIENT, "invalid_client", null, HttpStatus.BAD_REQUEST));
        }
        if (log.isTraceEnabled()) {
            log.trace("Retrieved registered client");
        }


        Optional<Authorization> optionalAuthorization = this.authorizationService.findByRefreshToken(refreshTokenAuthentication.getRefreshToken());

        if (optionalAuthorization.isEmpty()) {
            log.error("No authorization found for refresh token {}", refreshTokenAuthentication.getRefreshToken());
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, StringUtils.EMPTY, null, HttpStatus.UNAUTHORIZED));
        }

        OAuth2Authorization authorization = this.authorizationService.fromAuthorizationEntity(optionalAuthorization.get());

        if (authorization == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid request: refresh_token is invalid");
            }

            log.error("invalid token");
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, StringUtils.EMPTY, null, HttpStatus.UNAUTHORIZED));
        }

        if (log.isTraceEnabled()) {
            log.trace("Retrieved authorization with refresh token");
        }

        if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, INVALID_GRANT, null, HttpStatus.BAD_REQUEST));
        }

        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid request: requested grant_type is not allowed for registered client '{}'", registeredClient.getId());
            }

            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, "unauthorized client", null, HttpStatus.BAD_REQUEST));
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        if(refreshToken == null ){
            log.error("Invalid request: refresh_token is null");
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, StringUtils.EMPTY, null, HttpStatus.BAD_REQUEST));
        }

        if (!refreshToken.isActive()) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid request: refresh_token is not active for registered client '{}'", registeredClient.getId());
            }

            log.error("refresh token is not active");
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, StringUtils.EMPTY, null, HttpStatus.BAD_REQUEST));
        }

        Set<String> scopes = refreshTokenAuthentication.getScopes();
        Set<String> authorizedScopes = authorization.getAuthorizedScopes();
        if (!authorizedScopes.containsAll(scopes)) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_SCOPE, "invalid scope", null, HttpStatus.BAD_REQUEST));
        }

        //verifying if public key is provided
        Jwt dPoPProof = CustomDPoPProofVerifier.verifyIfAvailable(refreshTokenAuthentication);
        if (dPoPProof != null & clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
            Map<String, Object> accessTokenClaims = authorization.getAccessToken().getClaims();
            verifyDPoPProofPublicKey(dPoPProof, () -> accessTokenClaims);
        }

        if (log.isTraceEnabled()) {
            log.trace("Validated token request parameters");
        }

        if (scopes.isEmpty()) {
            scopes = authorizedScopes;
        }

        final String currentSessionId = optionalAuthorization.get().getSessionId();
        if(currentSessionId != null){
            UserCacheDto userCacheDto = cacheUtil.getData(currentSessionId, UserCacheDto.class);
            if(userCacheDto == null){
                throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_USER_SESSION, "invalid user session", null, HttpStatus.BAD_REQUEST));
            }

            //reactivate user cache
            cacheUtil.setGenericData(currentSessionId, new UserCacheDto(optionalAuthorization.get().getUsername()), false, 1, TimeUnit.HOURS);
        }


        UsernamePasswordAuthenticationToken lightWeightPrincipal =  authorization.getAttribute(Principal.class.getName());
        HashMap<String, Object> details = new HashMap<>();
        details.put(AuthConstants.SESSION_ID, currentSessionId);
        lightWeightPrincipal.setDetails(details);


        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(lightWeightPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorization)
                .authorizedScopes(scopes)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrant(refreshTokenAuthentication);

        if (dPoPProof != null) {
            tokenContextBuilder.put(OAuth2TokenContext.DPOP_PROOF_KEY, dPoPProof);
        }

        // ----- Access Token -----
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);
        OAuth2TokenContext tokenContext = (tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.SERVER_ERROR, "The token generator failed to generate the access token.", null, HttpStatus.INTERNAL_SERVER_ERROR));

        }

        if (log.isTraceEnabled()) {
            log.trace("Generated access token");
        }

        OAuth2AccessToken accessToken = CustomOAuth2AuthenticationProviderUtils.accessToken(authorizationBuilder, 
                generatedAccessToken, tokenContext);

        // ----- Refresh Token -----
        OAuth2RefreshToken currentRefreshToken = refreshToken.getToken();
        if (!registeredClient.getTokenSettings().isReuseRefreshTokens()) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .authorization(authorizationBuilder.build())
                    .build();
            
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the refresh token.", null, HttpStatus.INTERNAL_SERVER_ERROR));
            }

            if (log.isTraceEnabled()) {
                log.trace("Generated refresh token");
            }

            currentRefreshToken = (OAuth2RefreshToken) generatedRefreshToken;
            authorizationBuilder.refreshToken(currentRefreshToken);
        }

        // ----- ID token -----
        OidcIdToken idToken;
        if (authorizedScopes.contains(OidcScopes.OPENID)) {
            tokenContext = ((tokenContextBuilder.tokenType(AuthConstants.ID_TOKEN_TOKEN_TYPE))
                    .authorization(authorizationBuilder.build()))
                    .build();
            
            OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the ID token.", null, HttpStatus.INTERNAL_SERVER_ERROR));
            }

            if (log.isTraceEnabled()) {
                log.trace("Generated id token");
            }

            idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(), 
                    generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
            authorizationBuilder.token(idToken, 
                    metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
        } else {
            idToken = null;
        }

        this.saveAuthorization(authorizationBuilder, authorizedScopes, lightWeightPrincipal, optionalAuthorization.get().getUsername(),
                currentSessionId);

        if (log.isTraceEnabled()) {
            log.trace("Saved authorization");
        }

        Map<String, Object> additionalParameters = Collections.emptyMap();
        if (idToken != null) {
            additionalParameters = new HashMap<>();
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
        }

        if (log.isTraceEnabled()) {
            log.trace("Authenticated token request");
        }

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, currentRefreshToken, 
                additionalParameters);
        
    }

    @Override
    public boolean supports(@NotNull Class<?> authentication) {
        return OAuth2RefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void saveAuthorization(OAuth2Authorization.Builder authorizationBuilder, Set<String> authorizedScopes,
                                   Authentication lightWeightPrincipal, String username, String sessionId) {
        OAuth2Authorization authorization = authorizationBuilder
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), lightWeightPrincipal)
                .build();
        this.authorizationService.saveWithUserDetails(authorization, username, sessionId);
    }

    private static void verifyDPoPProofPublicKey(Jwt dPoPProof, ClaimAccessor accessTokenClaims) {
        JWK jwk = null;
        Map<String, Object> jwkJson = (Map)dPoPProof.getHeaders().get("jwk");

        try {
            jwk = JWK.parse(jwkJson);
        } catch (Exception _) {
        }

        if (jwk == null) {
            OAuth2Error error = new OAuth2Error(AuthConstants.INVALID_DPOP_PROOF, "jwk header is missing or invalid.", null);
            throw new OAuth2AuthenticationException(error);
        } else {
            String jwkThumbprint;
            try {
                jwkThumbprint = jwk.computeThumbprint().toString();
            } catch (Exception ex) {
                OAuth2Error error = new OAuth2Error(AuthConstants.INVALID_DPOP_PROOF, "Failed to compute SHA-256 Thumbprint for jwk.", null);
                throw new OAuth2AuthenticationException(error);
            }

            String jwkThumbprintClaim = null;
            Map<String, Object> confirmationMethodClaim = accessTokenClaims.getClaimAsMap("cnf");
            if (!CollectionUtils.isEmpty(confirmationMethodClaim) && confirmationMethodClaim.containsKey("jkt")) {
                jwkThumbprintClaim = (String)confirmationMethodClaim.get("jkt");
            }

            if (jwkThumbprintClaim == null) {
                OAuth2Error error = new OAuth2Error(AuthConstants.INVALID_DPOP_PROOF, "jkt claim is missing.", null);
                throw new OAuth2AuthenticationException(error);
            } else if (!jwkThumbprint.equals(jwkThumbprintClaim)) {
                OAuth2Error error = new OAuth2Error(AuthConstants.INVALID_DPOP_PROOF, "jwk header is invalid.", null);
                throw new OAuth2AuthenticationException(error);
            }
        }
    }
}

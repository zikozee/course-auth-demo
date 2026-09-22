package com.zee.courseauthdemo.config.customgrant;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.CustomUser;
import com.zee.courseauthdemo.dto.UserCacheDto;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.repository.impl.JpaAuthorizationService;
import com.zee.courseauthdemo.usermanagement.repository.SystemUserRepository;
import com.zee.courseauthdemo.usermanagement.service.UserService;
import com.zee.courseauthdemo.util.AuthConstants;
import com.zee.courseauthdemo.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @dev : Ezekiel Eromosei
 * @date : 17 Sep, 2026
 */

@Slf4j
public class CustomGrantAuthenticationProvider implements AuthenticationProvider {


    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final CacheUtil cacheUtil;
    private final JpaAuthorizationService authorizationService;
    private final UserService userService;
    private SessionRegistry sessionRegistry;

    public CustomGrantAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                                             OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator, CacheUtil cacheUtil,
                                             JpaAuthorizationService authorizationService, UserService userService) {

        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.notNull(passwordEncoder, "passwordEncoder must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        Assert.notNull(cacheUtil, "cacheUtil must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(userService, "userRepository must not be null");

        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.cacheUtil = cacheUtil;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @Override
    public @NotNull Authentication authenticate(@NotNull Authentication authentication) throws AuthenticationException {
        CustomGrantAuthenticationToken customGrantAuthentication = (CustomGrantAuthenticationToken) authentication;

        // Ensure the client is authenticated
        OAuth2ClientAuthenticationToken clientPrincipal = CustomOAuth2AuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(customGrantAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if(registeredClient == null) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_CLIENT, "invalid_client", null, HttpStatus.BAD_REQUEST));
        }

        final String username = customGrantAuthentication.getUsername();
        final String password = customGrantAuthentication.getPassword();
        final Set<String> authorizedScopes = customGrantAuthentication.getScope();

        if (!registeredClient.getAuthorizationGrantTypes().contains(customGrantAuthentication.getGrantType())) {
            log.error("unauthorized client");
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.UNAUTHORIZED_CLIENT, "Unauthorized client", null, HttpStatus.FORBIDDEN));
        }

        authorizedScopes.forEach(scope -> {
            if (!registeredClient.getScopes().contains(scope)) {
                throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE, "Invalid Scope", null, HttpStatus.BAD_REQUEST));
            }
        });

        CustomUser user = this.login(username, password);
        final String sessionId = UUID.randomUUID().toString();

        //todo add username and session id to MDC for logging/tracing
        MDC.put(AuthConstants.USER_ID, user.getUsername());
        MDC.put(AuthConstants.USER_SESSION_ID, sessionId);

        //cache session against user details
        cacheUtil.setGenericData(sessionId, new UserCacheDto(user.getUsername()), false, 1, TimeUnit.HOURS);

        // include session in principal so it can be injected in the claims
        UsernamePasswordAuthenticationToken lightWeightPrincipal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        HashMap<String, Object> details = new HashMap<>();
        details.put(AuthConstants.SESSION_ID, sessionId);
        lightWeightPrincipal.setDetails(details);

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(lightWeightPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(customGrantAuthentication.getGrantType())
                .authorizationGrant(customGrantAuthentication);

        // ----- Access Token -----
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.SERVER_ERROR,
                    "The token generator failed to generate the access token.", null, HttpStatus.INTERNAL_SERVER_ERROR));
        }

        if (log.isTraceEnabled()) {
            log.trace("Generated access token");
        }

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(clientPrincipal.getName())
                .authorizationGrantType(customGrantAuthentication.getGrantType());

        OAuth2AccessToken accessToken = CustomOAuth2AuthenticationProviderUtils.accessToken(authorizationBuilder,
                generatedAccessToken, tokenContext);

        // ----- Refresh Token -----
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
                && !clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (generatedRefreshToken != null) {
                 if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                     throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.SERVER_ERROR, "The token generator failed to generate the refresh token.", AuthConstants.ERROR_URI, HttpStatus.INTERNAL_SERVER_ERROR));
                 }
                 refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
                 authorizationBuilder.refreshToken(refreshToken);
            }
        }

        // ----- ID token -----
        OidcIdToken idToken;
        if (customGrantAuthentication.getScope().contains(OidcScopes.OPENID)) {
            SessionInformation sessionInformation = getSessionInformation(lightWeightPrincipal);
            if (sessionInformation != null) {
                try {
                    sessionInformation = new SessionInformation(sessionInformation.getPrincipal(),
                            createHash(sessionInformation.getSessionId()), sessionInformation.getLastRequest());
                } catch (NoSuchAlgorithmException ex) {
                    log.error("error computing hash for session id", ex);
                    throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.SERVER_ERROR, "Failed to compute hash for Session ID.", AuthConstants.ERROR_URI, HttpStatus.INTERNAL_SERVER_ERROR));
                }
                tokenContextBuilder.put(SessionInformation.class, sessionInformation);
            }
            tokenContext = tokenContextBuilder
                    .tokenType(AuthConstants.ID_TOKEN_TOKEN_TYPE)
                    .authorization(authorizationBuilder.build())
                    .build();
            OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.SERVER_ERROR, "The token generator failed to generate the ID token.", AuthConstants.ERROR_URI, HttpStatus.INTERNAL_SERVER_ERROR));
            }
            idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
                    generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
            authorizationBuilder.token(idToken,
                    metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
        } else {
            idToken = null;
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        if (idToken != null) {
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
        }

        // delete all old user sessions on login
        authorizationService.deleteUserOldActiveSessions(user.getUsername());

        this.saveAuthorization(authorizationBuilder, authorizedScopes, lightWeightPrincipal, username, sessionId);
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(@NotNull Class<?> authentication) {
        return CustomGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }



    private CustomUser login(String username, String password){
        CustomUser customUser = (CustomUser)userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, customUser.getPassword())) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INCORRECT_USERNAME_PASSWORD,
                    "Username, password details are incorrect.", null, HttpStatus.BAD_REQUEST));
        }

        if(!customUser.isEnabled()) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.USER_INACTIVE, HttpStatus.FORBIDDEN));
        }

        if(!customUser.isAccountNonLocked()) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.USER_LOCKED, HttpStatus.FORBIDDEN));
        }

        List<String> permissions = userService.getUserPermissionsByRole(customUser.getRole());
        return updateUserPermissions(customUser, permissions);
    }

    private CustomUser updateUserPermissions(CustomUser customUser, List<String> permissions) {
        return new CustomUser(
                customUser.getId(),
                customUser.getFullName(),
                customUser.getRole(),
                customUser.getEmail(),
                customUser.getUsername(),
                customUser.getPassword(),
                customUser.isEnabled(),
                customUser.isAccountNonLocked(),
                permissions.stream().map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
    }

    private void saveAuthorization(OAuth2Authorization.Builder authorizationBuilder, Set<String> authorizedScopes,
                                   Authentication principal, String username, String sessionId) {
        OAuth2Authorization authorization = authorizationBuilder
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), principal)
                .build();
        this.authorizationService.saveWithUserDetails(authorization, username, sessionId);
    }


    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }


    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        Assert.notNull(sessionRegistry, "sessionRegistry cannot be null");
        this.sessionRegistry = sessionRegistry;
    }


    private SessionInformation getSessionInformation(Authentication principal) {
        SessionInformation sessionInformation = null;
        if (this.sessionRegistry != null) {
            List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal.getPrincipal(), false);
            if (!CollectionUtils.isEmpty(sessions)) {
                sessionInformation = sessions.getFirst();
                if (sessions.size() > 1) {
                    // Get the most recent session
                    sessions = new ArrayList<>(sessions);
                    sessions.sort(Comparator.comparing(SessionInformation::getLastRequest));
                    sessionInformation = sessions.getLast();
                }
            }
        }
        return sessionInformation;
    }
}

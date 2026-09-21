package com.zee.courseauthdemo.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @dev : Ezekiel Eromosei
 * @date : 21 Sep, 2026
 * implemented from {@link org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AccessTokenResponseAuthenticationSuccessHandler}
 */

@Slf4j
public class CustomAccessTokenResponseHandler implements AuthenticationSuccessHandler {

    final OAuth2AccessTokenResponseHttpMessageConverter httpMessageConverter= new OAuth2AccessTokenResponseHttpMessageConverter();
    private Consumer<OAuth2AccessTokenAuthenticationContext> accessTokenResponseCustomizer;

    @Override
    public void onAuthenticationSuccess(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                        @NotNull Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AccessTokenAuthenticationToken accessTokenAuthentication)) {
            if (log.isErrorEnabled()) {
                log.error("{} must be of type {} but was {}", Authentication.class.getSimpleName(), OAuth2AccessTokenAuthenticationToken.class.getName(), authentication.getClass().getName());
            }
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "Unable to process the access token response.", null);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
        OAuth2RefreshToken refreshToken = accessTokenAuthentication.getRefreshToken();
        Map<String, Object> additionalParameters = accessTokenAuthentication.getAdditionalParameters();
        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                .tokenType(accessToken.getTokenType())
                .scopes(accessToken.getScopes());
        if (accessToken.getIssuedAt() != null && accessToken.getExpiresAt() != null) {
            builder.expiresIn(ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()));
        }
        if (refreshToken != null) {
            builder.refreshToken(refreshToken.getTokenValue());
        }
        if (!CollectionUtils.isEmpty(additionalParameters)) {
            builder.additionalParameters(additionalParameters);
        }

        if (this.accessTokenResponseCustomizer != null) {
            // @formatter:off
            OAuth2AccessTokenAuthenticationContext accessTokenAuthenticationContext =
                    OAuth2AccessTokenAuthenticationContext.with(accessTokenAuthentication)
                            .accessTokenResponse(builder)
                            .build();
            // @formatter:on
            this.accessTokenResponseCustomizer.accept(accessTokenAuthenticationContext);
            if (log.isTraceEnabled()) {
                log.trace("Customized access token response");
            }
        }

        OAuth2AccessTokenResponse accessTokenResponse = builder.build();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpMessageConverter.setAccessTokenResponseParametersConverter(new CustomOAuth2AccessTokenResponseMapConverter());
        httpMessageConverter.write(accessTokenResponse, null, httpResponse);
    }

    /**
     * Sets the {@code Consumer} providing access to the
     * {@link OAuth2AccessTokenAuthenticationContext} containing an
     * {@link OAuth2AccessTokenResponse.Builder} and additional context information.
     * @param accessTokenResponseCustomizer the {@code Consumer} providing access to the
     * {@link OAuth2AccessTokenAuthenticationContext} containing an
     * {@link OAuth2AccessTokenResponse.Builder}
     */
    public void setAccessTokenResponseCustomizer(
            Consumer<OAuth2AccessTokenAuthenticationContext> accessTokenResponseCustomizer) {
        Assert.notNull(accessTokenResponseCustomizer, "accessTokenResponseCustomizer cannot be null");
        this.accessTokenResponseCustomizer = accessTokenResponseCustomizer;
    }

}

package com.zee.courseauthdemo.config.logout;


import com.zee.courseauthdemo.config.ConverterUtil;
import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 *  * implemented from {@link org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2TokenRevocationAuthenticationConverter}
 */


public class CustomOAuth2TokenRevocationAuthenticationConverter implements AuthenticationConverter {

    @Override
    public @Nullable Authentication convert(@NotNull HttpServletRequest request) {

        final String token = request.getParameter(OAuth2ParameterNames.TOKEN);

        if (!StringUtils.hasText(token)) {
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_TOKEN, "TOKEN is missing", null, HttpStatus.BAD_REQUEST));
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");


        return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal, null);
    }
}

package com.zee.courseauthdemo.config.oauth2errorhandler;


import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 * implemented from {@link org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ErrorAuthenticationFailureHandler}
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2ErrorAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final HttpMessageConverter<CustomOAuth2Error> errorResponseConverter = new CustomOAuth2ErrorHttpMessageConverter();

    @Override
    public void onAuthenticationFailure(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull AuthenticationException authenticationException) throws IOException, ServletException {
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);

        if (authenticationException instanceof CustomOAuth2AuthenticationException exception) {
            CustomOAuth2Error error = exception.getError();
            httpResponse.setStatusCode(error.getHttpStatus());
            this.errorResponseConverter.write(error, null, httpResponse);
        } else {
            log.error("{} must be of type {}  but was {}", AuthenticationException.class.getSimpleName(),
                    CustomOAuth2AuthenticationException.class.getName(), authenticationException.getClass().getName());
        }
    }
}

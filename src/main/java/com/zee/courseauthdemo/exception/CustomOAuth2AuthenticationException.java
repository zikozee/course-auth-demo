package com.zee.courseauthdemo.exception;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

@Getter
public class CustomOAuth2AuthenticationException extends OAuth2AuthenticationException {

    private final CustomOAuth2Error error;
    public CustomOAuth2AuthenticationException(CustomOAuth2Error error) {
        super(new OAuth2Error(error.getErrorCode(), error.getDescription(), error.getUri()));
        this.error = error;
    }
}

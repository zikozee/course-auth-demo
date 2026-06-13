package com.zee.courseauthdemo.config.oauth2errorhandler;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

@Getter
public class CustomOAuth2Error extends OAuth2Error {
    private final HttpStatus httpStatus;

    public CustomOAuth2Error(String errorCode, HttpStatus httpStatus) {
        super(errorCode);
        this.httpStatus = httpStatus;
    }

    public CustomOAuth2Error(String errorCode, String description, String uri, HttpStatus httpStatus) {
        super(errorCode, description, uri);
        this.httpStatus = httpStatus;
    }
}

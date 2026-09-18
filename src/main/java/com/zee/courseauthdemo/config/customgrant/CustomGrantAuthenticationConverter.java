package com.zee.courseauthdemo.config.customgrant;


import com.zee.courseauthdemo.config.ConverterUtil;
import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @dev : Ezekiel Eromosei
 * @date : 17 Sep, 2026
 */

@Slf4j
public class CustomGrantAuthenticationConverter implements AuthenticationConverter {

    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {

        final String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if(!grantType.equals("custom_grant")) {
            // i safely assume this Authentication converter is the last on the list else it will block
            // e.g grantType: authorization_code, client_credentials and refresh_token
            // why because I am not returning null instead throwing an exception
            //Note:: if you desire another AuthenticationConverter to process the token request, return null here
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_GRANT_TYPE, "Invalid grant_type", request.getRequestURI(), HttpStatus.BAD_REQUEST));
        }

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        MultiValueMap<String, String> parameters = ConverterUtil.getFormParameters(request);

        Map<String, Object> additionalParameters = new HashMap<>();

        //move all params along except grantType
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE)) {
                additionalParameters.put(key, value.getFirst());
            }
        });

        return new CustomGrantAuthenticationToken(grantType, clientPrincipal, additionalParameters);
    }
}

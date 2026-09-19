package com.zee.courseauthdemo.config.customgrant;


import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * @dev : Ezekiel Eromosei
 * @date : 17 Sep, 2026
 */

public class CustomGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String username;
    @Getter
    private final String password;

    private final String scope;

    protected CustomGrantAuthenticationToken(String grantType, Authentication clientPrincipal, Map<String, Object> additionalParameters){
        super(new AuthorizationGrantType(grantType), clientPrincipal, additionalParameters);

        this.username = (String) additionalParameters.get("username");
        this.password = (String) additionalParameters.get("password");
        this.scope = (String)additionalParameters.get(OAuth2ParameterNames.SCOPE);
    }

    public Set<String> getScope(){
        return StringUtils.commaDelimitedListToSet(scope.replace(" ", ","));
    }

}

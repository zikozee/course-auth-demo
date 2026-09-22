package com.zee.courseauthdemo.config.customgrant;


import com.zee.courseauthdemo.usermanagement.permission.system.SystemPermissionSource;
import com.zee.courseauthdemo.util.AuthConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Component
public class CustomOAuth2TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication authenticationPrincipal = context.getPrincipal();
        Set<String> authorities = new HashSet<>();

        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            if(Objects.equals(context.getAuthorizationGrantType(), AuthorizationGrantType.CLIENT_CREDENTIALS)){
                authorities = Arrays.stream(SystemPermissionSource.values())
                        .map(SystemPermissionSource::getPermission)
                        .collect(Collectors.toSet());
            }
            if(Objects.equals(context.getAuthorizationGrantType(), new AuthorizationGrantType(AuthConstants.CUSTOM_GRANT_TYPE))){
                injectSessionIdToClaims(context, authenticationPrincipal);
                authorities = authenticationPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
            }
        }
        context.getClaims().claim(AuthConstants.AUTHORITIES, authorities);
        context.getClaims().claim(AuthConstants.IS_SYSTEM_TO_SYSTEM, String.valueOf(Objects.equals(context.getAuthorizationGrantType(), AuthorizationGrantType.CLIENT_CREDENTIALS)));

    }

    private void injectSessionIdToClaims(JwtEncodingContext context, Authentication authenticationPrincipal) {
        if(!(authenticationPrincipal instanceof AnonymousAuthenticationToken)
                && authenticationPrincipal.getDetails() instanceof HashMap){

                Map<String, Object> details = (HashMap<String, Object>) authenticationPrincipal.getDetails();
                if(details != null && !details.isEmpty()){
                    context.getClaims().claim(AuthConstants.SESSION_ID, details.get(AuthConstants.SESSION_ID));
                }
            }
    }
}

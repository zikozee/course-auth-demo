package com.zee.courseauthdemo.config.customgrant;


import com.zee.courseauthdemo.usermanagement.permission.system.SystemPermissionSource;
import com.zee.courseauthdemo.usermanagement.service.UserService;
import com.zee.courseauthdemo.util.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    private final UserService userService;

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
            if(Objects.equals(context.getAuthorizationGrantType(), AuthorizationGrantType.AUTHORIZATION_CODE)){
                log.info(new ObjectMapper().writeValueAsString(authenticationPrincipal));
                String username = resolveUsername(authenticationPrincipal);

                List<String> permissions = userService.getPermissionsByUsernameOrEmail(username);
                authorities = new HashSet<>(permissions);
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

    private String resolveUsername(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        // Google / OIDC login
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }

        // Username/password login
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return authentication.getName();
    }
}

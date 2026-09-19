package com.zee.courseauthdemo.config.refreshtoken;


import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.StringUtils;

/**
 * @dev : Ezekiel Eromosei
 * @date : 28 Jul, 2025
 */

public class CustomDPoPProofVerifier {
    private static final JwtDecoderFactory<DPoPProofContext> dPoPProofVerifierFactory = new DPoPProofJwtDecoderFactory();

    private CustomDPoPProofVerifier() {
    }

    static Jwt verifyIfAvailable(OAuth2AuthorizationGrantAuthenticationToken authorizationGrantAuthentication) {
        String dPoPProof = (String)authorizationGrantAuthentication.getAdditionalParameters().get("dpop_proof");
        if (!StringUtils.hasText(dPoPProof)) {
            return null;
        } else {
            String method = (String)authorizationGrantAuthentication.getAdditionalParameters().get("dpop_method");
            String targetUri = (String)authorizationGrantAuthentication.getAdditionalParameters().get("dpop_target_uri");

            try {
                DPoPProofContext dPoPProofContext = DPoPProofContext.withDPoPProof(dPoPProof).method(method).targetUri(targetUri).build();
                JwtDecoder dPoPProofVerifier = dPoPProofVerifierFactory.createDecoder(dPoPProofContext);
                return dPoPProofVerifier.decode(dPoPProof);
            } catch (Exception ex) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_dpop_proof"), ex);
            }
        }
    }
}

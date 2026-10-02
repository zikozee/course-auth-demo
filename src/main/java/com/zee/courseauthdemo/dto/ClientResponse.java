package com.zee.courseauthdemo.dto;

import lombok.Builder;

import java.util.List;

/**
 * @dev : Ezekiel Eromosei
 * @date : 02 Oct, 2026
 */


public record ClientResponse(List<ClientDto> allClients) {
    @Builder
    public record ClientDto(
            String clientId,
            String clientName,
            List<String> authenticationMethods,
            List<String> grantTypes,

            List<String> redirectUris,
            List<String> postLogoutRedirectUris,
            List<String> scopes,

            int accessTokenTtL,
            int refreshTokenTtl,
            boolean requiresProofKey,
            List<String> audience
    ) { }
}

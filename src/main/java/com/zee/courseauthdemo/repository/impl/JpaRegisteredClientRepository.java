package com.zee.courseauthdemo.repository.impl;


import com.zee.courseauthdemo.entity.Client;
import com.zee.courseauthdemo.repository.ClientRepository;
import com.zee.courseauthdemo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @dev : Ezekiel Eromosei
 * @date : 01 Jun, 2026
 */

@Repository
@RequiredArgsConstructor
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");
        this.clientRepository.save(buildFromRegisteredClient(registeredClient));
    }

    @Override
    public @Nullable RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        return this.clientRepository.findById(Long.valueOf(id)).map(this::buildFromClient).orElse(null);
    }

    @Override
    public @Nullable RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        return this.clientRepository.findByClientId(clientId).map(this::buildFromClient).orElse(null);
    }

    private Client buildFromRegisteredClient(RegisteredClient registeredClient) {
        Optional<Client> optionalClient = clientRepository.findById(Long.valueOf(registeredClient.getId()));

        if (optionalClient.isPresent()) {
            return optionalClient.get();
        }

        List<String> clientAuthenticationMethods = new ArrayList<>(registeredClient.getClientAuthenticationMethods().size());
        registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
                clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));

        List<String> authorizationGrantTypes = new ArrayList<>(registeredClient.getAuthorizationGrantTypes().size());
        registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
                authorizationGrantTypes.add(authorizationGrantType.getValue()));

        Client entity = new Client();
        entity.setId(Long.valueOf(registeredClient.getId()));
        entity.setClientId(registeredClient.getClientId());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientName(registeredClient.getClientName());
        entity.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
        entity.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
        entity.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
        entity.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getPostLogoutRedirectUris()));
        entity.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
        entity.setAccessTokenTimeToLiveInMinutes((int)registeredClient.getTokenSettings().getAccessTokenTimeToLive().toMinutes());
        entity.setRefreshTokenTimeToLiveInMinutes((int)registeredClient.getTokenSettings().getRefreshTokenTimeToLive().toMinutes());
        return entity;
    }

    private RegisteredClient buildFromClient(Client client) {
        Set<String> clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
                client.getClientAuthenticationMethods());
        Set<String> authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
                client.getAuthorizationGrantTypes());
        Set<String> redirectUris = StringUtils.commaDelimitedListToSet(
                client.getRedirectUris());
        Set<String> postLogoutRedirectUris = StringUtils.commaDelimitedListToSet(
                client.getPostLogoutRedirectUris());
        Set<String> clientScopes = StringUtils.commaDelimitedListToSet(
                client.getScopes());

        RegisteredClient.Builder builder = RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientName(client.getClientName())
                .clientAuthenticationMethods(authenticationMethods ->
                        clientAuthenticationMethods.forEach(authenticationMethod ->
                                authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
                .authorizationGrantTypes(grantTypes -> authorizationGrantTypes.forEach(grantType ->
                        grantTypes.add(AuthUtil.resolveAuthorizationGrantType(grantType))))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .postLogoutRedirectUris(uris -> uris.addAll(postLogoutRedirectUris))
                .scopes(scopes -> scopes.addAll(clientScopes));

        // made modifications here as opposed to what we have on https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html
        builder.tokenSettings(TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofMinutes(client.getAccessTokenTimeToLiveInMinutes()))
                .refreshTokenTimeToLive(Duration.ofMinutes(client.getRefreshTokenTimeToLiveInMinutes()))
                .reuseRefreshTokens(false)
                .build()
        );

        return builder.build();
    }


    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        } else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.NONE;
        }
        return new ClientAuthenticationMethod(clientAuthenticationMethod);      // Custom client authentication method
    }
}

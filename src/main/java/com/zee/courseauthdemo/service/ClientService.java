package com.zee.courseauthdemo.service;


import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2Error;
import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.ClientResponse;
import com.zee.courseauthdemo.entity.Client;
import com.zee.courseauthdemo.exception.CustomOAuth2AuthenticationException;
import com.zee.courseauthdemo.repository.ClientRepository;
import com.zee.courseauthdemo.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @dev : Ezekiel Eromosei
 * @date : 29 Sep, 2026
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    public static final String AUDIENCE_CACHE_KEY = "AUDIENCE_CACHE_KEY_";


    private final ClientRepository clientRepository;
    private final CacheUtil cacheUtil;


    public List<String> getAudiencesByClientId(String clientId){
        List<String> cachedAudience = cacheUtil.getDataList(AUDIENCE_CACHE_KEY + clientId, String.class);
        if(cachedAudience != null && !cachedAudience.isEmpty()){
            return cachedAudience;
        }

        Optional<Client> optionalClient = clientRepository.findByClientId(clientId);
        if(optionalClient.isEmpty()){
            log.info("Invalid client with clientId: {}", clientId);
            throw new CustomOAuth2AuthenticationException(new CustomOAuth2Error(ErrorCodeConstants.INVALID_CLIENT, HttpStatus.UNAUTHORIZED));
        }

        List<String> audiences = Arrays.asList(optionalClient.get().getAudience().split(","));
        cacheUtil.setGenericData(AUDIENCE_CACHE_KEY + clientId, audiences, false, 24, TimeUnit.HOURS);
        return audiences;
    }

    public ClientResponse getAllClients(){

        return new ClientResponse(
                clientRepository.findAll()
                        .stream()
                        .map(client ->
                                ClientResponse.ClientDto.builder()
                                        .clientId(client.getClientId())
                                        .clientName(client.getClientName())
                                        .authenticationMethods(toList(client.getClientAuthenticationMethods()))
                                        .grantTypes(toList(client.getAuthorizationGrantTypes()))
                                        .redirectUris(toList(client.getRedirectUris()))
                                        .postLogoutRedirectUris(toList(client.getPostLogoutRedirectUris()))
                                        .scopes(toList(client.getScopes()))
                                        .accessTokenTtL(client.getAccessTokenTimeToLiveInMinutes())
                                        .refreshTokenTtl(client.getRefreshTokenTimeToLiveInMinutes())
                                        .requiresProofKey(client.isRequiresProofKey())
                                        .audience(toList(client.getAudience()))
                                        .build()
                        )
                        .toList()
        );
    }


    private List<String> toList(String data){
        return Arrays.asList(data.split(","));
    }
}

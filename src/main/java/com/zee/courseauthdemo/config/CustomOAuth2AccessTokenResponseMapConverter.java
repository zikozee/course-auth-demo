package com.zee.courseauthdemo.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.util.AuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * @dev : Ezekiel Eromosei
 * @date : 21 Sep, 2026
 *  * implemented from {@link org.springframework.security.oauth2.core.endpoint.DefaultOAuth2AccessTokenResponseMapConverter}
 */

@Slf4j
final class CustomOAuth2AccessTokenResponseMapConverter implements Converter<OAuth2AccessTokenResponse, Map<String, Object>> {


    @Override
    public Map<String, Object> convert(OAuth2AccessTokenResponse tokenResponse) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(AuthConstants.ACCESS_TOKEN, tokenResponse.getAccessToken().getTokenValue());
        parameters.put(AuthConstants.TOKEN_TYPE, tokenResponse.getAccessToken().getTokenType().getValue());
        parameters.put(AuthConstants.EXPIRES_IN, getExpiresIn(tokenResponse));

        if (tokenResponse.getRefreshToken() != null) {
            parameters.put(AuthConstants.REFRESH_TOKEN, tokenResponse.getRefreshToken().getTokenValue());
        }

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .successful(true)
                .messageType(MessageType.SUCCESS)
                .data(parameters)
                .build();

        final Gson gson = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        final String json = gson.toJson(apiResponse);

        return gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private static long getExpiresIn(OAuth2AccessTokenResponse tokenResponse) {
        if (tokenResponse.getAccessToken().getExpiresAt() != null) {
            return ChronoUnit.SECONDS.between(Instant.now(), tokenResponse.getAccessToken().getExpiresAt());
        }
        return -1;
    }
}

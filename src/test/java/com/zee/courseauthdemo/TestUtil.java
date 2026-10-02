package com.zee.courseauthdemo;

import com.zee.courseauthdemo.testdto.PkceParameters;
import com.zee.courseauthdemo.testdto.TokenResponse;
import com.zee.courseauthdemo.testdto.TokenData;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

final class TestUtil {

    static final String BASE_URL = "http://127.0.0.1:8080";
    static final String REDIRECT_URI = "http://127.0.0.1:5173/callback";
    static final String USERNAME = "user";
    static final String PASSWORD = "password";
    private static final String AUTHORIZATION_STATE = "authorization-code-test";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JwtDecoder jwtDecoder;
    private final RegisteredClientRepository registeredClientRepository;

    TestUtil(WebApplicationContext context, ObjectMapper objectMapper, JwtDecoder jwtDecoder,
             RegisteredClientRepository registeredClientRepository) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        this.objectMapper = objectMapper;
        this.jwtDecoder = jwtDecoder;
        this.registeredClientRepository = registeredClientRepository;
    }

    PkceParameters generatePkceParameters() throws Exception {
        byte[] verifierBytes = new byte[32];
        new SecureRandom().nextBytes(verifierBytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        return new PkceParameters(verifier, challenge);
    }

    void assertRegisteredRedirectUri(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        assertThat(client).as("Registered client %s", clientId).isNotNull();
        assertThat(client.getRedirectUris()).as("Registered redirect URIs").contains(REDIRECT_URI);
    }

    MockHttpSession authorize(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult authorization = mockMvc.perform(request
                        .accept(MediaType.TEXT_HTML)
                        .queryParam("state", AUTHORIZATION_STATE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) authorization.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    String loginAndGetAuthorizationCode(MockHttpSession session) throws Exception {
        mockMvc.perform(get(BASE_URL + "/login").session(session))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post(BASE_URL + "/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", USERNAME)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String savedAuthorizationUrl = login.getResponse().getRedirectedUrl();
        session = (MockHttpSession) login.getRequest().getSession(false);

        assertThat(savedAuthorizationUrl).isNotNull().startsWith(BASE_URL + "/oauth2/authorize?");
        assertThat(session).isNotNull();

        MvcResult callback = mockMvc.perform(get(URI.create(savedAuthorizationUrl))
                        .session(session)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String callbackUrl = callback.getResponse().getRedirectedUrl();
        assertThat(callbackUrl).isNotNull().startsWith(REDIRECT_URI + "?");
        MultiValueMap<String, String> query = UriComponentsBuilder.fromUriString(callbackUrl).build().getQueryParams();
        assertThat(query).doesNotContainKey("error");
        assertThat(query.getFirst("state")).isEqualTo(AUTHORIZATION_STATE);
        String code = query.getFirst("code");
        assertThat(code).isNotBlank();

        return code;
    }

    void assertTokenResponse(MockHttpServletRequestBuilder request, String expectedSubject,
                             boolean expectsRefreshToken) throws Exception {
        MvcResult tokenResult = mockMvc.perform(request
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        TokenResponse response = objectMapper.readValue(
                tokenResult.getResponse().getContentAsByteArray(), TokenResponse.class);
        assertThat(response.successful()).isTrue();
        assertThat(response.messageType()).isEqualTo("SUCCESS");
        assertThat(response.data()).isNotNull();

        TokenData token = response.data();
        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isPositive();
        assertThat(token.accessToken()).isNotBlank();
        if (expectsRefreshToken) {
            assertThat(token.refreshToken()).isNotBlank();
        } else {
            assertThat(token.refreshToken()).isNull();
        }

        assertJwt(token.accessToken(), expectedSubject);
    }

    private void assertJwt(String accessToken, String expectedSubject) {
        // Decode with the application's public key to verify the JWT signature.
        Jwt jwt = jwtDecoder.decode(accessToken);
        Instant now = Instant.now();
        assertThat(jwt.getExpiresAt()).isNotNull().isAfter(now);
        assertThat(jwt.getIssuedAt()).isNotNull().isBeforeOrEqualTo(now);
        if (jwt.getNotBefore() != null) {
            assertThat(jwt.getNotBefore()).isBeforeOrEqualTo(now);
        }
        assertThat(jwt.getAudience()).contains("payment-service", "reporting-service");
        assertThat(jwt.getSubject()).isEqualTo(expectedSubject);
    }
}

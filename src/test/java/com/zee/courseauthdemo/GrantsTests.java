package com.zee.courseauthdemo;

import com.zee.courseauthdemo.testdto.PkceParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static com.zee.courseauthdemo.TestUtil.BASE_URL;
import static com.zee.courseauthdemo.TestUtil.REDIRECT_URI;
import static com.zee.courseauthdemo.TestUtil.USERNAME;
import static com.zee.courseauthdemo.TestUtil.PASSWORD;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @dev : Ezekiel Eromosei
 * @date : 02 Oct, 2026
 */

@SpringBootTest
class GrantsTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    private TestUtil testUtil;

    @BeforeEach
    void setUp() {
        testUtil = new TestUtil(webApplicationContext, objectMapper, jwtDecoder, registeredClientRepository);
    }

    @Test
    void testAuthorizationCode() throws Exception {
        MockHttpSession session = testUtil.authorize(get(BASE_URL + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "oidc-client")
                .queryParam("scope", "openid")
                .queryParam("redirect_uri", REDIRECT_URI));
        String code = testUtil.loginAndGetAuthorizationCode(session);

        MockHttpServletRequestBuilder tokenRequest = post(BASE_URL + "/oauth2/token")
                .with(httpBasic("oidc-client", "secret"))
                .formField("grant_type", "authorization_code")
                .formField("scope", "openid profile")
                .formField("code", code)
                .formField("redirect_uri", REDIRECT_URI);
        testUtil.assertTokenResponse(tokenRequest, USERNAME, true);
    }

    @Test
    void testPkce() throws Exception {
        PkceParameters pkce = testUtil.generatePkceParameters();
        testUtil.assertRegisteredRedirectUri("oidc-client2");

        MockHttpSession session = testUtil.authorize(get(BASE_URL + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "oidc-client2")
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("scope", "openid")
                .queryParam("code_challenge", pkce.challenge())
                .queryParam("code_challenge_method", "S256"));
        String code = testUtil.loginAndGetAuthorizationCode(session);

        MockHttpServletRequestBuilder tokenRequest = post(BASE_URL + "/oauth2/token")
                .with(httpBasic("oidc-client2", "secret2"))
                .formField("grant_type", "authorization_code")
                .formField("scope", "openid profile")
                .formField("code", code)
                .formField("redirect_uri", REDIRECT_URI)
                .formField("code_verifier", pkce.verifier());
        testUtil.assertTokenResponse(tokenRequest, USERNAME, true);
    }

    @Test
    void clientCredentials() throws Exception {
        MockHttpServletRequestBuilder tokenRequest = post(BASE_URL + "/oauth2/token")
                .with(httpBasic("oidc-client3", "secret3"))
                .formField("grant_type", "client_credentials")
                .formField("scope", "openid");
        testUtil.assertTokenResponse(tokenRequest, "oidc-client3", false);
    }

    @Test
    void testCustomGrant() throws Exception {
        MockHttpServletRequestBuilder tokenRequest = post(BASE_URL + "/oauth2/token")
                .with(httpBasic("oidc-client4", "secret4"))
                .formField("grant_type", "custom_oauth_grant")
                .formField("scope", "openid profile")
                .formField("username", USERNAME)
                .formField("password", PASSWORD);
        testUtil.assertTokenResponse(tokenRequest, USERNAME, true);
    }
}

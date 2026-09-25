package com.zee.courseauthdemo.config;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.zee.courseauthdemo.config.customgrant.CustomGrantAuthenticationConverter;
import com.zee.courseauthdemo.config.customgrant.CustomGrantAuthenticationProvider;
import com.zee.courseauthdemo.config.logout.CustomOAuth2TokenRevocationAuthenticationConverter;
import com.zee.courseauthdemo.config.logout.CustomOAuth2TokenRevocationAuthenticationProvider;
import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2ErrorAuthenticationFailureHandler;
import com.zee.courseauthdemo.config.refreshtoken.CustomRefreshTokenAuthenticationProvider;
import com.zee.courseauthdemo.repository.impl.JpaAuthorizationService;
import com.zee.courseauthdemo.service.CustomUserDetailsService;
import com.zee.courseauthdemo.usermanagement.service.UserService;
import com.zee.courseauthdemo.util.AuthConstants;
import com.zee.courseauthdemo.util.CacheUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @dev : Ezekiel Eromosei
 * @date : 25 Jan, 2026
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${private-key.name}")
    private String privateKeyName;

    @Value("${private-key.password}")
    private String privateKeyPassword;

    @Value("${private-key.alias}")
    private String privateKeyAlias;

    @Value("${private-key.id}")
    private String privateKeyId;

    @Value("${custom.logout-endpoint}")
    private String customLogoutEndpoint;




    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      CustomOAuth2ErrorAuthenticationFailureHandler authenticationFailureHandler,
                                                                      CustomUserDetailsService customUserDetailsService,
                                                                      PasswordEncoder passwordEncoder,
                                                                      OAuth2TokenGenerator<?> tokenGenerator,
                                                                      CacheUtil cacheUtil,
                                                                      JpaAuthorizationService authorizationService,
                                                                      UserService userService
                                                                      ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http
                .cors(Customizer.withDefaults())
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
                                .tokenEndpoint(tokenEndpoint ->
                                        tokenEndpoint
                                                .accessTokenRequestConverters(converters ->
                                                        converters.add(new CustomGrantAuthenticationConverter())
                                                )
                                                .authenticationProviders(providers -> {
                                                    providers.removeIf(OAuth2RefreshTokenAuthenticationProvider.class::isInstance);
                                                    providers.add(new CustomGrantAuthenticationProvider(
                                                            customUserDetailsService, passwordEncoder,
                                                            tokenGenerator, cacheUtil, authorizationService,
                                                            userService
                                                    ));
                                                    providers.add(new CustomRefreshTokenAuthenticationProvider(
                                                            authorizationService, cacheUtil, tokenGenerator)
                                                    );
                                                })
                                                .accessTokenResponseHandler(new CustomAccessTokenResponseHandler())
                                                .errorResponseHandler(authenticationFailureHandler)
                                )
                                .tokenRevocationEndpoint(revoke ->
                                        revoke
                                                .revocationRequestConverter(new CustomOAuth2TokenRevocationAuthenticationConverter())
                                                .authenticationProvider(
                                                        new CustomOAuth2TokenRevocationAuthenticationProvider(
                                                                authorizationService, cacheUtil
                                                        )
                                                )
                                                .errorResponseHandler(authenticationFailureHandler)
                                )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, @Value("${public.paths}") String publicPaths,
                                                          OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService) throws Exception {
        http
//                .formLogin(AbstractHttpConfigurer::disable) // to disable form login if choose to use custom grant alone
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                )
                .authorizeHttpRequests(authorize -> {

                    if(StringUtils.hasText(publicPaths)) {
                        Arrays.stream(publicPaths.split(","))
                                .forEach(path -> authorize.requestMatchers(path.trim()).permitAll());
                    }

                    authorize.anyRequest().authenticated();
                })
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin(Customizer.withDefaults())
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(oidcUserService)));

        return http.build();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(new ClassPathResource(privateKeyName), privateKeyPassword.toCharArray());

        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(privateKeyAlias);

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey key = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(privateKeyId)
                .build();

        JWKSet jwkSet = new JWKSet(key);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }


    @Bean
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer,
                                                     JWKSource<SecurityContext> jwkSource) {

        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        jwtGenerator.setJwtCustomizer(oAuth2TokenCustomizer);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }


    /**
     *  * default revocation endpoint can be found in
     *  {@link org.springframework.security.oauth2.server.authorization.web.OAuth2TokenRevocationEndpointFilter}
     */

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();

        // you can customize token endpoint here

        if(StringUtils.hasText(customLogoutEndpoint))
            builder.tokenRevocationEndpoint(customLogoutEndpoint);
        return builder.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * {@link org.springframework.security.config.oauth2.client.CommonOAuth2Provider}
     */
    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${oauth2.login.google.client-id}") String googleClientId,
            @Value("${oauth2.login.google.client-secret}") String googleClientSecret) {

        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE
                .getBuilder(AuthConstants.GOOGLE_PROVIDER)
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
//                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/google") //already inferred see CommonOAuth2Provider.DEFAULT_REDIRECT_URL
                .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }



    /**
     *  Resolves issue with AuthenticationTime when generating token in OAuth2AuthorizationCodeAuthenticationProvider during social login
     */
    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {

        OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            OidcUser user = delegate.loadUser(userRequest);
            Set<GrantedAuthority> authorities = new HashSet<>(user.getAuthorities());
            authorities.add(FactorGrantedAuthority.fromFactor(AuthConstants.GOOGLE_PROVIDER));

            return new DefaultOidcUser(
                    authorities,
                    user.getIdToken(),
                    user.getUserInfo(),
                    IdTokenClaimNames.SUB
            );
        };
    }
}

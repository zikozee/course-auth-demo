package com.zee.courseauthdemo.config;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.zee.courseauthdemo.config.customgrant.CustomGrantAuthenticationConverter;
import com.zee.courseauthdemo.config.customgrant.CustomGrantAuthenticationProvider;
import com.zee.courseauthdemo.config.oauth2errorhandler.CustomOAuth2ErrorAuthenticationFailureHandler;
import com.zee.courseauthdemo.repository.impl.JpaAuthorizationService;
import com.zee.courseauthdemo.service.CustomUserDetailsService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      CustomOAuth2ErrorAuthenticationFailureHandler authenticationFailureHandler,
                                                                      CustomUserDetailsService customUserDetailsService,
                                                                      PasswordEncoder passwordEncoder,
                                                                      OAuth2TokenGenerator<?> tokenGenerator,
                                                                      CacheUtil cacheUtil,
                                                                      JpaAuthorizationService authorizationService
                                                                      ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http
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
                                                    providers.add(new CustomGrantAuthenticationProvider(
                                                            customUserDetailsService, passwordEncoder,
                                                            tokenGenerator, cacheUtil, authorizationService
                                                    ));
                                                })
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
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin(Customizer.withDefaults());

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

    //todo - implement JwtCustomizer
    @Bean
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(/*OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer,*/
                                                     JWKSource<SecurityContext> jwkSource) {

        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
//        jwtGenerator.setJwtCustomizer(oAuth2TokenCustomizer);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

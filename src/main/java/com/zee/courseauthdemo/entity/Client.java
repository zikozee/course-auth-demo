package com.zee.courseauthdemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : Ezekiel Eromosei
 * @code @created : 01 Jun, 2026
 */

@Getter
@Setter
@Entity
@Table(name = "client")
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String clientId;
    @Column(length = 100, nullable = false)
    private String clientSecret;
    @Column(length = 100)
    private String clientName;
    @Column(name="client_auth_method",length = 20)
    private String clientAuthenticationMethods;
    @Column(name="auth_grant_types", length = 100)
    private String authorizationGrantTypes;
    @Column(length = 500)
    private String redirectUris;
    @Column(length = 1000)
    private String postLogoutRedirectUris;
    @Column(length = 100)
    private String scopes;
    @Column(name="access_token_ttl", length = 3)
    private int accessTokenTimeToLiveInMinutes;
    @Column(name="refresh_token_ttl", length = 2)
    private int refreshTokenTimeToLiveInMinutes;
    private boolean requiresProofKey;
}

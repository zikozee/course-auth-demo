package com.zee.courseauthdemo.repository;

import com.zee.courseauthdemo.entity.Authorization;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author : Ezekiel Eromosei
 * @code @created : 17 Dec, 2024
 */

@Repository
public interface AuthorizationRepository extends CrudRepository<Authorization, UUID> {

    Optional<Authorization> findByState(String state);
    Optional<Authorization> findByAuthorizationCodeValue(String authorizationCode);
    Optional<Authorization> findByAccessTokenValue(String accessToken);
    Optional<Authorization> findByRefreshTokenValue(String refreshToken);
    Optional<Authorization> findByOidcIdTokenValue(String idToken);
    List<Authorization> findAllByUsernameAndAccessTokenExpiresAtAfter(String username, Instant expiresAt);

    @Query("select a from Authorization a where a.state = :token" +
            " or a.authorizationCodeValue = :token" +
            " or a.accessTokenValue = :token" +
            " or a.refreshTokenValue = :token" +
            " or a.oidcIdTokenValue = :token"
    )
    Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValueOrOidcIdTokenValueOrUserCodeValueOrDeviceCodeValue(@Param("token") String token);


    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM authorizations a WHERE a.username=?1 and a.access_token_issued_at < ?2")
    void deleteAllPreviousUserSessions(String username, Instant accessTokenIssuedAtBefore);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM authorizations a WHERE a.access_token_expires_at< CURRENT_TIMESTAMP")
    void deleteAllExpiredAccessToken();
}

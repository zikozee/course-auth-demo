# Authorization Code
http://127.0.0.1:8080/oauth2/authorize?response_type=code&client_id=oidc-client&scope=openid&redirect_uri=https://spring.io

## classes to debug
- org.springframework.security.oauth2.server.authorization.authentication
  - OAuth2AuthorizationCodeRequestAuthenticationToken
  - OAuth2AuthorizationCodeRequestAuthenticationProvider
  - OAuth2AuthorizationCodeAuthenticationToken
  - OAuth2AuthorizationCodeAuthenticationProvider
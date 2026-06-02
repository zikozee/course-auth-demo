# Authorization Code
http://127.0.0.1:8080/oauth2/authorize?response_type=code&client_id=oidc-client&scope=openid&state=xyz&redirect_uri=https://spring.io

## Flow Steps - see Diagram
	1.	App redirects user to Authorization Server
	2.	User logs in & consents
	3.	Server redirects back with authorization code
	4.	Backend exchanges code + client secret for tokens


## hidden classes to debug
- org.springframework.security.oauth2.server.authorization.authentication
  - ClientSecretAuthenticationProvider 
  - OAuth2AuthorizationCodeRequestAuthenticationToken
  - OAuth2AuthorizationCodeRequestAuthenticationProvider
  - OAuth2AuthorizationCodeAuthenticationToken
  - OAuth2AuthorizationCodeAuthenticationProvider

## When to use
 - web app with a backend
 - Server-side web apps
 - Can securely store client_secret



# Proof Key for Code Exchange (PKCE) - see Diagram
PKCE prevents authorization code interception.
•	code_verifier → secret (kept by client)
•	code_challenge → hash sent to server

http://127.0.0.1:8080/oauth2/authorize?response_type=code&client_id=oidc-client2&redirect_uri=https://spring.io&scope=openid&code_challenge=tGRW-m62ss83EcHTGwut_jSNnRFh5bLA_ftu22Ka1ko&code_challenge_method=S256

- verifier: 5BJ5XdpFxhxYYtio_ejZap7GT1gnE2SpcNsDxFvAbRY

## hidden classes to debug
- org.springframework.security.oauth2.server.authorization.authentication
  - ClientSecretAuthenticationProvider
  - OAuth2AuthorizationCodeRequestAuthenticationToken
  - OAuth2AuthorizationCodeRequestAuthenticationProvider
  - OAuth2AuthorizationCodeAuthenticationToken
  - OAuth2AuthorizationCodeAuthenticationProvider


## When to use
  - SPAs- Single Page Applications (React, Angular, Vue)
  - Mobile apps
  - Desktop apps



# Refresh Token
- used to generate another access-token and refresh token

## hidden classes to debug
  - ClientSecretAuthenticationProvider
  - OAuth2RefreshTokenAuthenticationToken
  - OAuth2RefreshTokenAuthenticationProvider

## When to use
  - for authorization code token refresh
  - for pkce token refresh


# Client Credentials

## hidden classes to debug
  - ClientSecretAuthenticationProvider
  - OAuth2ClientCredentialsAuthenticationToken
  - OAuth2ClientCredentialsAuthenticationProvider
  
  - helper classes
    - OAuth2ClientCredentialsAuthenticationContext
    - OAuth2ClientCredentialsAuthenticationValidator


# Custom grant
- create Authorization and Client Entity
- create respective repository
- create respective jpa implementations
- move registered client to jpa Registered Repository (hard code)
- initialize properties to talk to the database
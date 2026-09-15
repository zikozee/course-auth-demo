INSERT INTO public.users (deleted, enabled, locked, created_date, deleted_date, updated_date, created_by, role, updated_by, password, username, email, full_name)

VALUES (false, true, false, '2026-09-15 13:39:42.793000 +00:00', null,
        null, 'User', 'Regular', 'User',
        '$2a$10$8Pu5NY8kJy3BIli2oo4/tOjTWP2Jh2sohi/tgge6gp0VT2giEFjxq',
        'user', 'test@test.com', 'Some User') ON CONFLICT (username) DO NOTHING;

-- encoded password = password

INSERT INTO public.client (
    access_token_ttl,
    refresh_token_ttl,
    requires_proof_key,
    created_date,
    client_auth_method,
    auth_grant_types,
    client_id,
    client_secret,
    scopes,
    client_name,
    redirect_uris,
    post_logout_redirect_uris,
    created_by,
    updated_by,
    updated_date,
    deleted,
    deleted_date
)
VALUES
(
    20,
    10,
    true,
    '2026-09-15 13:43:49.961000 +00:00',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'oidc-client2',
    '$2a$10$QAFM5t3w/qEmilHZ6OFDGukOTGHQX6qkyXmXRgnShxZrbS8NErofO',
    'openid,profile',
    'client2',
    'https://spring.io',
    'http://127.0.0.1:8080/',
    'User',
    null,
    null,
    false,
    null
),
(
    20,
    10,
    false,
    '2026-09-15 13:43:49.961000 +00:00',
    'client_secret_basic',
    'client_credentials',
    'oidc-client3',
    '$2a$10$Lhfi764oqNwaaBF/e9aW0.TmUkbKFrO16i12Vw0R0YtL2dSB.F.jK',
    'openid,profile',
    'client3',
    'https://spring.io',
    'http://127.0.0.1:8080/',
    'User',
    null,
    null,
    false,
    null
),
(
    20,
    10,
    false,
    '2026-09-15 13:43:49.961000 +00:00',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'oidc-client',
    '$2a$10$QNJ/yzQtiWhrsdFWq0jA4uU/DmP/X71uiddK9/v1KwPYrYfFYCh2K',
    'openid,profile',
    'client1',
    'https://spring.io',
    'http://127.0.0.1:8080/',
    'User',
    null,
    null,
    false,
    null
) ON CONFLICT (client_name) DO NOTHING;
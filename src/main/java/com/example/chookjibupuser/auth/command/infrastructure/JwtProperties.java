package com.example.chookjibupuser.auth.command.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationSeconds,
        String cookieName,
        boolean cookieSecure,
        String cookieSameSite
) {
}

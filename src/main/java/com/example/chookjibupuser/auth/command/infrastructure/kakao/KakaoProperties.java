package com.example.chookjibupuser.auth.command.infrastructure.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오 로그인(REST API) 연동 설정. client-id는 Kakao Developers의 REST API 키,
 * redirect-uri는 카카오 콘솔에 등록한 Redirect URI와 정확히 같아야 한다.
 */
@ConfigurationProperties(prefix = "app.kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}

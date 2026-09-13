package com.example.chookjibupuser.auth.command.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 비밀번호 재설정 이메일에 담을 링크를 만들 때 쓰는 프론트엔드 주소.
 * 로컬은 http://localhost:3000, 배포는 https://user.chookjibup.store로 환경마다
 * 다르게 설정해야 한다(application-secret.yml 또는 환경변수 FRONTEND_BASE_URL).
 */
@ConfigurationProperties(prefix = "app.frontend")
public record AppFrontendProperties(
        String baseUrl
) {
}
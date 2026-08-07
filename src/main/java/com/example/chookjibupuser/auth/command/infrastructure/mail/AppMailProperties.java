package com.example.chookjibupuser.auth.command.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 이메일 발신자 설정. SMTP 접속정보(host/port/username/password)는 Spring Boot가
 * spring.mail.* 에서 자동으로 읽어서 JavaMailSender 빈을 만들어주므로 여기서
 * 따로 다루지 않는다 — 여긴 "발신자로 보여줄 주소"만 담는다.
 */
@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(
        String from
) {
}

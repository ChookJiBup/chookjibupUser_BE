package com.example.chookjibupuser.passwordreset;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 비밀번호 재설정 링크 토큰 한 건. {@code password_reset_token} 테이블에 매핑한다.
 *
 * <p>이메일 인증코드(EmailVerificationCode)와 목적은 비슷하지만(이메일로 뭔가를 보내고
 * 검증), 방식이 다르다 — 인증코드는 사용자가 직접 6자리를 "입력"하는 거고, 이건
 * 이메일에 담긴 "링크를 클릭"하는 거라 토큰 값 자체가 추측 불가능해야 한다(UUID).
 * 그래서 emailverification 도메인과 별도 도메인으로 분리했다.</p>
 *
 * <p>passwordreset 도메인은 user 엔티티를 전혀 모른다 — userId를 숫자로만 들고 있는다.</p>
 */
@Entity
@Getter
@Table(name = "password_reset_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    private static final Duration VALID_DURATION = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "token", nullable = false, updatable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", insertable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    private PasswordResetToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public static PasswordResetToken issue(Long userId) {
        // UUID는 128비트 난수라 추측이 사실상 불가능하다 — 링크에 그대로 노출돼도 안전하다.
        return new PasswordResetToken(UUID.randomUUID().toString(), userId);
    }

    public boolean isExpired() {
        return expiresAt == null || OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    /** 비밀번호 변경 완료 처리. 트랜잭션 안에서 호출하면 더티체킹으로 자동 반영된다. */
    public void markUsed() {
        this.usedAt = OffsetDateTime.now();
    }
}
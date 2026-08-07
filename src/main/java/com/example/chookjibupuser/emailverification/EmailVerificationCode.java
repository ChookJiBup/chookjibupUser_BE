package com.example.chookjibupuser.emailverification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 이메일 인증 코드 한 건. 파이썬 파이프라인(schema.sql)이 만들어둔
 * {@code user_email_verification} 테이블에 매핑한다.
 *
 * <p>{@code created_at}/{@code expires_at}는 DB 기본값(now(), now()+15분)이 채워주므로
 * insertable=false로 두고 조회 시에만 읽어온다 — TIMESTAMPTZ 컬럼이라 LocalDateTime이
 * 아니라 OffsetDateTime으로 매핑한다(그렇지 않으면 Hibernate가 'timestamp'로 기대해서
 * ddl-auto: validate에서 타입 불일치가 난다. FestivalWishlist에서 겪었던 것과 동일한
 * 이유다).</p>
 */
@Entity
@Getter
@Table(name = "user_email_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationCode {

    private static final int MAX_ATTEMPTS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "code", nullable = false, updatable = false)
    private String code;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", insertable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "attempt_count", insertable = false)
    private Integer attemptCount;

    private EmailVerificationCode(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public static EmailVerificationCode issue(String email, String code) {
        return new EmailVerificationCode(email, code);
    }

    public boolean isExpired() {
        return expiresAt == null || OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isAttemptLimitExceeded() {
        return attemptCount != null && attemptCount >= MAX_ATTEMPTS;
    }

    public boolean matchesCode(String inputCode) {
        return this.code.equals(inputCode);
    }

    /** 코드가 틀렸을 때 시도 횟수를 늘린다. 트랜잭션 안에서 호출하면 더티체킹으로 자동 반영된다. */
    public void increaseAttempt() {
        this.attemptCount = (this.attemptCount == null ? 0 : this.attemptCount) + 1;
    }

    /** 인증 성공 처리. 트랜잭션 안에서 호출하면 더티체킹으로 자동 반영된다. */
    public void markVerified() {
        this.verifiedAt = OffsetDateTime.now();
    }
}

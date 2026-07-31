package com.example.chookjibupuser.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 사용자 계정이다. 파이썬 파이프라인(schema.sql)이 이미 만들어둔
 * {@code users} 테이블에 그대로 매핑한다 — 이 서버는 테이블을 만들지 않는다
 * (application.yml의 ddl-auto: validate 참고).
 *
 * <p>로그인 아이디/비밀번호 컬럼은 테이블에 없다 — kakao_id가 유일한 로그인 식별자이다.
 * joined_at/is_withdrawn/withdrawn_at/updated_at 컬럼은 DB 기본값/트리거가 알아서
 * 채워주므로 이 엔티티에서는 매핑하지 않았다(컴팩트하게 유지).</p>
 */
@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // DB 컬럼 자체는 nullable입니다 (레거시 마이그레이션 중 남아있을 수 있는 행 때문에
    // NOT NULL 제약을 걸지 않았습니다 — schema.sql 참고). "카카오 로그인 시 반드시 값이
    // 있어야 한다"는 보장은 UserAccount.createFromKakao()에서 애플리케이션 레벨로 합니다.
    @Column(name = "kakao_id", updatable = false)
    private Long kakaoId;

    @Column(name = "nickname", length = 100)
    private String nickname;

    // 카카오 계정 이메일 제공에 동의하지 않은 사용자도 있을 수 있어 nullable이다.
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private UserAccount(Long kakaoId, String nickname, String email, String profileImageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * 카카오 사용자 정보로 신규 계정을 생성한다.
     */
    public static UserAccount createFromKakao(
            Long kakaoId,
            String nickname,
            String email,
            String profileImageUrl
    ) {
        if (kakaoId == null || kakaoId <= 0) {
            throw new IllegalArgumentException("kakaoId는 필수입니다.");
        }
        return new UserAccount(kakaoId, nickname, email, profileImageUrl);
    }

    /**
     * 재로그인 시 카카오 쪽 최신 프로필로 갱신한다.
     */
    public void syncKakaoProfile(String nickname, String email, String profileImageUrl) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
}

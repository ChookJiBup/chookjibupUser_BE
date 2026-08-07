package com.example.chookjibupuser.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 계정이다. 파이썬 파이프라인(schema.sql)이 이미 만들어둔 {@code users}
 * 테이블에 그대로 매핑한다 — 이 서버는 테이블을 만들지 않는다
 * (application.yml의 ddl-auto: validate 참고).
 *
 * <p>카카오 로그인과 이메일/비밀번호 로그인 두 가지를 다 지원한다. {@code loginType}으로
 * 어느 쪽인지 구분한다 — DB에도 같은 의미의 CHECK 제약(chk_users_login_type_consistency)이
 * 있어서, kakao면 kakaoId가, email이면 passwordHash가 반드시 있어야 한다.</p>
 *
 * <p>joined_at/is_withdrawn/withdrawn_at/updated_at 컬럼은 DB 기본값/트리거가 알아서
 * 채워주므로 이 엔티티에서는 매핑하지 않았다(컴팩트하게 유지).</p>
 */
@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    private static final String LOGIN_TYPE_KAKAO = "kakao";
    private static final String LOGIN_TYPE_EMAIL = "email";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_type", nullable = false, length = 20, updatable = false)
    private String loginType;

    // DB 컬럼 자체는 nullable입니다 (이메일 가입자는 카카오 ID가 없습니다).
    @Column(name = "kakao_id", updatable = false)
    private Long kakaoId;

    // 이메일 로그인 사용자만 값이 있습니다 (bcrypt 해시). 카카오 사용자는 항상 null입니다.
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "nickname", length = 100)
    private String nickname;

    // 카카오 계정 이메일 제공에 동의하지 않은 사용자도 있을 수 있어 nullable이다.
    // 이메일 로그인 사용자는 회원가입 시 필수로 채워진다(애플리케이션 레벨 보장).
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    // 카카오 형식(YYYY/MMDD 분리)을 그대로 따른다 — 이메일 가입 때도 같은 포맷으로 저장해서
    // 두 로그인 방식이 같은 컬럼을 공유하게 했다.
    @Column(name = "birthyear", length = 4)
    private String birthyear;

    @Column(name = "birthday", length = 4)
    private String birthday;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private UserAccount(
            String loginType,
            Long kakaoId,
            String passwordHash,
            String nickname,
            String email,
            String phoneNumber,
            String birthyear,
            String birthday,
            String profileImageUrl
    ) {
        this.loginType = loginType;
        this.kakaoId = kakaoId;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthyear = birthyear;
        this.birthday = birthday;
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
        return new UserAccount(
                LOGIN_TYPE_KAKAO, kakaoId, null, nickname, email, null, null, null, profileImageUrl
        );
    }

    /**
     * 이메일/비밀번호로 신규 계정을 생성한다. 회원가입 API(EmailSignupService)에서만 호출되고,
     * 그 시점엔 이미 이메일 인증이 끝난 상태여야 한다 — 이 팩토리 메서드 자체는 인증 여부를
     * 검증하지 않는다(그건 EmailSignupService의 책임).
     *
     * @param passwordHash 평문이 아니라 이미 해싱된 값이어야 한다 (PasswordEncoder.encode 결과).
     */
    public static UserAccount createFromEmail(
            String email,
            String passwordHash,
            String nickname,
            String phoneNumber,
            String birthyear,
            String birthday
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 필수입니다.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash는 필수입니다.");
        }
        return new UserAccount(
                LOGIN_TYPE_EMAIL, null, passwordHash, nickname, email, phoneNumber, birthyear, birthday, null
        );
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

    public boolean isEmailLogin() {
        return LOGIN_TYPE_EMAIL.equals(this.loginType);
    }
}

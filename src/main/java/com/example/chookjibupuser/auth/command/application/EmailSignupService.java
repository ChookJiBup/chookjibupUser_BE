package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.api.auth.dto.EmailSignupRequest;
import com.example.chookjibupuser.api.auth.dto.UserLoginResponse;
import com.example.chookjibupuser.auth.command.infrastructure.JwtTokenProvider;
import com.example.chookjibupuser.emailverification.EmailVerificationService;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.user.UserAccount;
import com.example.chookjibupuser.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 이메일/비밀번호 회원가입 유스케이스를 처리한다. emailverification 도메인(인증 여부 확인)과
 * user 도메인(계정 생성)을 엮는 지점이다.
 */
@Service
@RequiredArgsConstructor
public class EmailSignupService {

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("MMdd");

    private final EmailVerificationService emailVerificationService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserLoginResponse signup(EmailSignupRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new CustomException(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        }
        if (!emailVerificationService.isVerified(request.email())) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED);
        }
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_ALREADY_REGISTERED);
        }

        UserAccount userAccount = UserAccount.createFromEmail(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.phoneNumber(),
                toBirthyear(request.birthDate()),
                toBirthday(request.birthDate())
        );
        UserAccount saved = userAccountRepository.save(userAccount);

        String accessToken = jwtTokenProvider.createAccessToken(saved);
        return UserLoginResponse.of(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                true,
                saved
        );
    }

    private String toBirthyear(LocalDate birthDate) {
        return birthDate == null ? null : birthDate.format(YEAR_FORMAT);
    }

    private String toBirthday(LocalDate birthDate) {
        return birthDate == null ? null : birthDate.format(MONTH_DAY_FORMAT);
    }
}

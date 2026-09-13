package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.auth.command.infrastructure.mail.AppFrontendProperties;
import com.example.chookjibupuser.auth.command.infrastructure.mail.PasswordResetMailSender;
import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import com.example.chookjibupuser.passwordreset.PasswordResetTokenService;
import com.example.chookjibupuser.user.UserAccount;
import com.example.chookjibupuser.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 재설정 유스케이스(링크 발송 → 링크 클릭 후 변경)를 처리한다.
 * passwordreset 도메인(토큰 발급/검증), user 도메인(계정 조회/비밀번호 변경),
 * 메일 발송 인프라를 엮는 지점이다.
 *
 * <p>[설계 메모] "가입 안 된 이메일이어도 성공한 것처럼 응답해서 계정 존재 여부를
 * 숨겨야 하나?" 고민했는데, 이 프로젝트의 다른 이메일 API(회원가입의
 * AUTH_EMAIL_ALREADY_REGISTERED)도 이미 이메일 존재 여부를 그대로 알려주는 식이라,
 * 여기서만 다르게 숨기면 오히려 일관성이 깨진다고 판단해서 똑같이 명확한 에러를
 * 던지게 했다. 나중에 정책이 바뀌면 requestReset()의 예외 처리 부분만 고치면 된다.</p>
 */
@Service
@RequiredArgsConstructor
public class PasswordResetRequestService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetMailSender passwordResetMailSender;
    private final AppFrontendProperties frontendProperties;
    private final PasswordEncoder passwordEncoder;

    /**
     * 가입한 이메일로 비밀번호 재설정 링크를 보낸다. 카카오 로그인 계정(비밀번호 자체가
     * 없음)이면 재설정할 대상이 없으므로 거부한다.
     */
    @Transactional
    public void requestReset(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!userAccount.isEmailLogin()) {
            throw new CustomException(ErrorCode.AUTH_PASSWORD_RESET_NOT_EMAIL_ACCOUNT);
        }

        String token = passwordResetTokenService.issueToken(userAccount.getUserId());
        String resetUrl = frontendProperties.baseUrl().replaceAll("/+$", "") + "/reset-password?token=" + token;
        passwordResetMailSender.sendResetLink(email, resetUrl);
    }

    /**
     * 이메일로 받은 링크의 token과 새 비밀번호로 실제 비밀번호를 바꾼다.
     * 토큰은 1회용이라, 여기서 성공하든 실패(만료/이미 사용됨)하든 재사용할 수 없다.
     */
    @Transactional
    public void confirmReset(String token, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new CustomException(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        }

        Long userId = passwordResetTokenService.consumeToken(token);
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userAccount.changePassword(passwordEncoder.encode(newPassword));
    }
}
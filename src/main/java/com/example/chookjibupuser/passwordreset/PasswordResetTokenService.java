package com.example.chookjibupuser.passwordreset;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 재설정 토큰 발급/검증을 처리한다. passwordreset 도메인 자신의 저장소만
 * 다룬다 — "이 이메일이 실제 가입된 계정인지", "새 비밀번호로 실제 바꾸는 것"은
 * application 계층(PasswordResetRequestService)의 책임이다.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository repository;

    /** 새 토큰을 발급해서 저장한다. 토큰 문자열 자체를 돌려주고, 실제 메일 발송은 호출하는 쪽의 책임이다. */
    @Transactional
    public String issueToken(Long userId) {
        PasswordResetToken saved = repository.save(PasswordResetToken.issue(userId));
        return saved.getToken();
    }

    /**
     * 토큰이 유효한지 확인하고, 유효하면 즉시 "사용됨"으로 표시한다(1회용 링크라 재사용 방지).
     *
     * @return 토큰 주인의 userId
     */
    @Transactional
    public Long consumeToken(String token) {
        PasswordResetToken resetToken = repository.findByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_NOT_FOUND));

        if (resetToken.isUsed()) {
            throw new CustomException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_ALREADY_USED);
        }
        if (resetToken.isExpired()) {
            throw new CustomException(ErrorCode.AUTH_PASSWORD_RESET_TOKEN_EXPIRED);
        }

        resetToken.markUsed();
        return resetToken.getUserId();
    }
}
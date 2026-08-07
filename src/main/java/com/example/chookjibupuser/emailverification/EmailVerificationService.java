package com.example.chookjibupuser.emailverification;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * 이메일 인증 코드 발급/검증을 처리한다. emailverification 도메인 자신의 저장소만
 * 다룬다 — user 도메인(회원가입)은 전혀 모른다. "인증된 이메일로만 회원가입 허용"은
 * application 계층(EmailSignupService)의 책임이다.
 *
 * <p>코드 자체가 만료되는 것과 별개로, 실제 DB에서 지우는 배치(user_email_verification_cleaner.py,
 * 파이썬 파이프라인 쪽)가 15분마다 따로 돈다 — 여기서는 지우지 않고 만료 여부만 확인한다.</p>
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationCodeRepository repository;

    /**
     * 새 인증 코드를 발급해서 저장한다. 코드 값 자체를 돌려주고, 실제 메일 발송은
     * 호출하는 쪽(EmailVerificationRequestService)의 책임이다 — 이 도메인은 메일 발송
     * 방법(SMTP 등)을 전혀 모른다.
     */
    @Transactional
    public String issueCode(String email) {
        String code = generateCode();
        repository.save(EmailVerificationCode.issue(email, code));
        return code;
    }

    /**
     * 입력한 코드가 맞는지 확인하고, 맞으면 인증완료로 표시한다.
     * 틀리면 시도 횟수를 늘리고 예외를 던진다.
     */
    @Transactional
    public void verifyCode(String email, String inputCode) {
        EmailVerificationCode latest = repository.findTopByEmailOrderByVerificationIdDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_EMAIL_VERIFICATION_NOT_FOUND));

        if (latest.isExpired()) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_VERIFICATION_EXPIRED);
        }
        if (latest.isAttemptLimitExceeded()) {
            throw new CustomException(ErrorCode.AUTH_EMAIL_VERIFICATION_TOO_MANY_ATTEMPTS);
        }
        if (!latest.matchesCode(inputCode)) {
            latest.increaseAttempt();
            throw new CustomException(ErrorCode.AUTH_EMAIL_VERIFICATION_CODE_MISMATCH);
        }
        latest.markVerified();
    }

    /**
     * 이 이메일이 (아직 만료 전 상태로) 인증완료 처리가 됐는지 확인한다.
     * 회원가입 직전에 EmailSignupService가 호출한다.
     */
    public boolean isVerified(String email) {
        return repository.findTopByEmailOrderByVerificationIdDesc(email)
                .map(EmailVerificationCode::isVerified)
                .orElse(false);
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", value);
    }
}

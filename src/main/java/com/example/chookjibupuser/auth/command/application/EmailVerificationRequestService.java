package com.example.chookjibupuser.auth.command.application;

import com.example.chookjibupuser.auth.command.infrastructure.mail.VerificationMailSender;
import com.example.chookjibupuser.emailverification.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증코드 발송 유스케이스를 처리한다. emailverification 도메인(코드 발급)과
 * 메일 발송 인프라를 엮는 지점이다.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationRequestService {

    private final EmailVerificationService emailVerificationService;
    private final VerificationMailSender mailSender;

    public void sendVerificationCode(String email) {
        String code = emailVerificationService.issueCode(email);
        mailSender.sendVerificationCode(email, code);
    }

    public void confirmVerificationCode(String email, String code) {
        emailVerificationService.verifyCode(email, code);
    }
}

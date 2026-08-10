package com.example.chookjibupuser.auth.command.infrastructure.mail;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 이메일 인증코드를 실제로 발송한다. spring.mail.* (application-secret.yml)에 SMTP
 * 정보가 채워져 있어야 동작한다 — 안 채웠으면 발송 시점에 예외가 난다.
 *
 * <p>로컬 개발 중 SMTP를 아직 안 붙였어도 흐름을 테스트할 수 있도록, 발송 성공/실패와
 * 무관하게 코드를 로그에도 남긴다. 실제 운영 환경에서는 이 로그가 인증코드를 그대로
 * 노출하므로, 로그 수집기에 그대로 쌓이지 않도록 운영 배포 시엔 로그 레벨을 조정하거나
 * 이 줄을 지우는 걸 권장한다 (README에도 명시).</p>
 *
 * <p>클래스명을 {@code MailSender}가 아니라 {@code VerificationMailSender}로 둔다 —
 * Spring Boot의 {@code MailSenderAutoConfiguration}이 만드는 {@code JavaMailSender}
 * 빈 이름도 클래스명 규칙상 "mailSender"가 되어, {@code @Component}로 등록되는
 * 클래스명을 그대로 두면 빈 이름이 충돌해 기동이 실패한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationMailSender {

    private final JavaMailSender javaMailSender;
    private final AppMailProperties mailProperties;

    public void sendVerificationCode(String toEmail, String code) {
        // 로컬 테스트 편의용 — 운영에서는 로그에 인증코드가 남지 않도록 조정 권장 (README 참고).
        log.info("[VerificationMailSender] {} 로 인증코드 발송 시도 (code={})", toEmail, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(toEmail);
        message.setSubject("[축제지법] 이메일 인증번호");
        message.setText("인증번호: " + code + "\n15분 이내에 입력해주세요.");

        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            log.error("[VerificationMailSender] {} 발송 실패", toEmail, exception);
            throw new CustomException(ErrorCode.AUTH_EMAIL_SEND_FAILED);
        }
    }
}

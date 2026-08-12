package com.example.chookjibupuser.auth.command.infrastructure.mail;

import com.example.chookjibupuser.global.response.CustomException;
import com.example.chookjibupuser.global.response.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    private static final String SUBJECT = "[축지법] 이메일 인증번호";
    private static final String BRAND_COLOR = "#FD9E4F";

    private final JavaMailSender javaMailSender;
    private final AppMailProperties mailProperties;

    public void sendVerificationCode(String toEmail, String code) {
        // 로컬 테스트 편의용 — 운영에서는 로그에 인증코드가 남지 않도록 조정 권장 (README 참고).
        log.info("[VerificationMailSender] {} 로 인증코드 발송 시도 (code={})", toEmail, code);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(toEmail);
            helper.setSubject(SUBJECT);
            helper.setText(buildPlainText(code), buildHtml(code));

            javaMailSender.send(message);
        } catch (MailException | MessagingException exception) {
            log.error("[VerificationMailSender] {} 발송 실패", toEmail, exception);
            throw new CustomException(ErrorCode.AUTH_EMAIL_SEND_FAILED);
        }
    }

    /** HTML을 못 여는 메일 클라이언트를 위한 대체 텍스트. */
    private String buildPlainText(String code) {
        return "[축지법] 이메일 인증번호\n\n인증번호: " + code + "\n15분 이내에 입력해주세요.";
    }

    private String buildHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <body style="margin:0; padding:0; background-color:#f4f5f7;
                             font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="background-color:#f4f5f7; padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="480" cellpadding="0" cellspacing="0"
                               style="background-color:#ffffff; border-radius:12px; overflow:hidden;
                                      box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                          <tr>
                            <td style="background-color:%1$s; padding:28px 32px;">
                              <span style="color:#ffffff; font-size:20px; font-weight:700;">
                                축지법
                              </span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 32px 12px 32px;">
                              <p style="margin:0 0 8px 0; color:#111827; font-size:18px; font-weight:700;">
                                이메일 인증번호
                              </p>
                              <p style="margin:0; color:#6B7280; font-size:14px; line-height:1.6;">
                                아래 인증번호를 입력해서 이메일 인증을 완료해주세요.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px 8px 32px;">
                              <div style="background-color:#FFF3E8; border-radius:8px;
                                          padding:20px; text-align:center;">
                                <span style="font-size:32px; font-weight:800; letter-spacing:8px;
                                             color:%1$s;">
                                  %2$s
                                </span>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 32px 32px 32px;">
                              <p style="margin:0; color:#9CA3AF; font-size:13px; line-height:1.6;">
                                인증번호는 발급 후 <strong style="color:#6B7280;">15분간</strong> 유효합니다.<br>
                                본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 32px; background-color:#FAFAFA;
                                       border-top:1px solid #EEEEEE;">
                              <p style="margin:0; color:#B0B4BA; font-size:12px;">
                                본 메일은 발신 전용입니다. 문의사항은 앱 내 고객센터를 이용해주세요.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(BRAND_COLOR, code);
    }
}
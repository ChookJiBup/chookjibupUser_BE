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
 * 비밀번호 재설정 링크를 실제로 발송한다. VerificationMailSender(인증코드 메일)와
 * SMTP 설정(spring.mail.*)을 그대로 공유하고, HTML 톤도 통일했다 — 목적이 달라서
 * (코드 입력 vs 링크 클릭) 별도 클래스로 뒀다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetMailSender {

    private static final String SUBJECT = "[축지법] 비밀번호 재설정 안내";
    private static final String BRAND_COLOR = "#FD9E4F";

    private final JavaMailSender javaMailSender;
    private final AppMailProperties mailProperties;

    public void sendResetLink(String toEmail, String resetUrl) {
        // 로컬 테스트 편의용 — 운영에서는 로그에 링크(=토큰)가 남지 않도록 조정 권장.
        log.info("[PasswordResetMailSender] {} 로 비밀번호 재설정 링크 발송 시도 (url={})", toEmail, resetUrl);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(toEmail);
            helper.setSubject(SUBJECT);
            helper.setText(buildPlainText(resetUrl), buildHtml(resetUrl));

            javaMailSender.send(message);
        } catch (MailException | MessagingException exception) {
            log.error("[PasswordResetMailSender] {} 발송 실패", toEmail, exception);
            throw new CustomException(ErrorCode.AUTH_EMAIL_SEND_FAILED);
        }
    }

    private String buildPlainText(String resetUrl) {
        return "[축지법] 비밀번호 재설정\n\n아래 링크에서 새 비밀번호를 설정해주세요.\n" + resetUrl
                + "\n\n이 링크는 30분간, 1회만 유효합니다.\n본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.";
    }

    private String buildHtml(String resetUrl) {
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
                                비밀번호 재설정
                              </p>
                              <p style="margin:0; color:#6B7280; font-size:14px; line-height:1.6;">
                                아래 버튼을 눌러 새 비밀번호를 설정해주세요.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px 8px 32px; text-align:center;">
                              <a href="%2$s"
                                 style="display:inline-block; background-color:%1$s; color:#ffffff;
                                        font-size:15px; font-weight:700; text-decoration:none;
                                        padding:14px 32px; border-radius:8px;">
                                비밀번호 재설정하기
                              </a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 32px 32px 32px;">
                              <p style="margin:0; color:#9CA3AF; font-size:13px; line-height:1.6;">
                                버튼이 안 눌리면 아래 주소를 복사해서 브라우저에 붙여넣어주세요.<br>
                                <a href="%2$s" style="color:%1$s;">%2$s</a><br><br>
                                이 링크는 발급 후 <strong style="color:#6B7280;">30분간, 1회만</strong> 유효합니다.<br>
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
                """.formatted(BRAND_COLOR, resetUrl);
    }
}
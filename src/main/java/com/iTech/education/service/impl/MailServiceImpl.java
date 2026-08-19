package com.iTech.education.service.impl;

import com.iTech.education.service.MailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public MailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                           @Value("${spring.mail.username:}") String from,
                           @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.mailSender = mailSender.getIfAvailable();
        this.from = from;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    @Override
    @Async
    public void sendPaymentSuccess(String toEmail, String fullName, List<String> courseTitles,
                                   BigDecimal totalAmount, Long firstCourseId) {
        if (!canSend() || !StringUtils.hasText(toEmail)) {
            log.warn("Bỏ qua gửi mail xác nhận thanh toán (chưa cấu hình spring.mail.username)");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Thanh toán khóa học thành công — LearnHub");
            helper.setText(buildHtml(fullName, courseTitles, totalAmount, firstCourseId), true);
            mailSender.send(message);
            log.info("Đã gửi mail xác nhận thanh toán tới {}", toEmail);
        } catch (Exception ex) {
            log.error("Không gửi được mail xác nhận thanh toán tới {}", toEmail, ex);
        }
    }

    @Override
    @Async
    public void sendRegistrationSuccess(String toEmail, String fullName, String verifyUrl) {
        if (!canSend() || !StringUtils.hasText(toEmail)) {
            log.warn("Bỏ qua gửi mail đăng ký (chưa cấu hình spring.mail.username)");
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Đăng ký thành công — xác thực email LearnHub");
            helper.setText(buildRegisterHtml(fullName, verifyUrl), true);
            mailSender.send(message);
            log.info("Đã gửi mail xác thực đăng ký tới {}", toEmail);
        } catch (Exception ex) {
            log.error("Không gửi được mail xác thực đăng ký tới {}", toEmail, ex);
        }
    }

    private boolean canSend() {
        return mailSender != null && StringUtils.hasText(from);
    }

    private String buildHtml(String fullName, List<String> courseTitles, BigDecimal totalAmount, Long firstCourseId) {
        String courses = courseTitles.stream()
                .map(title -> "<li style=\"margin:6px 0;\">" + escape(title) + "</li>")
                .collect(Collectors.joining());
        String amount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalAmount);
        String learnUrl = firstCourseId != null ? frontendUrl + "/learn/" + firstCourseId : frontendUrl + "/learn";
        String name = StringUtils.hasText(fullName) ? escape(fullName) : "bạn";

        return """
                <div style="font-family:Arial,sans-serif;background:#07090e;padding:32px;color:#e5e7eb;">
                  <div style="max-width:560px;margin:0 auto;background:#12151c;border-radius:16px;padding:28px;border:1px solid #222;">
                    <p style="color:#f97316;font-size:12px;letter-spacing:2px;font-weight:700;margin:0;">LEARNHUB</p>
                    <h1 style="color:#ffffff;font-size:22px;margin:12px 0 16px;">Thanh toán khóa học thành công</h1>
                    <p>Xin chào %s,</p>
                    <p>LearnHub đã nhận thanh toán và ghi danh các khóa sau:</p>
                    <ul style="padding-left:18px;">%s</ul>
                    <p><strong style="color:#f97316;">Tổng tiền:</strong> %s</p>
                    <p style="margin:24px 0;">
                      <a href="%s" style="background:#f97316;color:#fff;text-decoration:none;padding:12px 20px;border-radius:10px;font-weight:700;">
                        Vào học ngay
                      </a>
                    </p>
                    <p style="color:#9ca3af;font-size:12px;margin-top:28px;">Email này được gửi tự động sau khi thanh toán VNPay thành công.</p>
                  </div>
                </div>
                """.formatted(name, courses, amount, learnUrl);
    }

    private String buildRegisterHtml(String fullName, String verifyUrl) {
        String name = StringUtils.hasText(fullName) ? escape(fullName) : "bạn";
        return """
                <div style="font-family:Arial,sans-serif;background:#07090e;padding:32px;color:#e5e7eb;">
                  <div style="max-width:560px;margin:0 auto;background:#12151c;border-radius:16px;padding:28px;border:1px solid #222;">
                    <p style="color:#f97316;font-size:12px;letter-spacing:2px;font-weight:700;margin:0;">LEARNHUB</p>
                    <h1 style="color:#ffffff;font-size:22px;margin:12px 0 16px;">Đăng ký thành công</h1>
                    <p>Xin chào %s,</p>
                    <p>Tài khoản LearnHub của bạn đã được tạo. Nhấn nút bên dưới để xác thực email và bắt đầu học.</p>
                    <p style="margin:24px 0;">
                      <a href="%s" style="background:#f97316;color:#fff;text-decoration:none;padding:12px 20px;border-radius:10px;font-weight:700;">
                        Xác thực email
                      </a>
                    </p>
                    <p style="color:#9ca3af;font-size:12px;">Link có hiệu lực 24 giờ. Nếu bạn không đăng ký, hãy bỏ qua email này.</p>
                  </div>
                </div>
                """.formatted(name, verifyUrl);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

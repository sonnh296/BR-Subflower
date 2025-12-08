package com.hls.sunflower.service.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hls.sunflower.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Xác minh tài khoản ElSun");

            String verificationLink = frontendUrl + "/auth/verify-email?token=" + verificationToken;

            String emailBody = String.format(
                    "Xin chào %s,\n\n" + "Cảm ơn bạn đã đăng ký tài khoản tại ElSun!\n\n"
                            + "Vui lòng click vào link dưới đây để xác minh tài khoản của bạn:\n\n"
                            + "%s\n\n"
                            + "Link xác minh này sẽ hết hạn sau 24 giờ.\n\n"
                            + "Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.\n\n"
                            + "Trân trọng,\n"
                            + "Đội ngũ ElSun",
                    username, verificationLink);

            message.setText(emailBody);
            mailSender.send(message);

            log.info("✅ Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.warn("⚠️ Failed to send verification email to: {} - Email service may not be configured. Error: {}",
                    to, e.getMessage());
            log.info("💡 User registration will continue without email verification. To enable email, please configure SMTP settings in application.yml");
            // Don't throw exception - allow registration to continue without email verification
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Chào mừng đến với ElSun!");

            String emailBody = String.format(
                    "Xin chào %s,\n\n" + "Tài khoản của bạn đã được xác minh thành công!\n\n"
                            + "Bạn có thể đăng nhập và bắt đầu mua sắm tại ElSun ngay bây giờ.\n\n"
                            + "Truy cập: %s\n\n"
                            + "Chúc bạn có trải nghiệm mua sắm tuyệt vời!\n\n"
                            + "Trân trọng,\n"
                            + "Đội ngũ ElSun",
                    username, frontendUrl);

            message.setText(emailBody);
            mailSender.send(message);

            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to: {} - Email service may not be configured. Error: {}",
                    to, e.getMessage());
            // Don't throw exception for welcome email failure
        }
    }
}

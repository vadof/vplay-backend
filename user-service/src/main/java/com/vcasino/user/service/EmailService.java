package com.vcasino.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // TODO configure to send emails faster
    @Async
    public void send(String to, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        try {
            helper.setText(content, true);
            helper.setTo(to);
            helper.setSubject("Activate Your VCasino Account Now");
            helper.setFrom("no-reply@vcasino.com");
            mailSender.send(mimeMessage);
            log.debug("Email sent to {}", to);
        } catch (MessagingException e) {
            // TODO exception?
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException(e);
        }
    }
}

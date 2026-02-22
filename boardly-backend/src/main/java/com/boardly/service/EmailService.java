package com.boardly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmailVerificationEmail(String to, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String link = frontendUrl + "/verify-email?token=" + token;
            String emailContent = "<h1>Email Verification</h1><p>Click the link below to verify your email address:</p><a href=\"" + link + "\">Verify Email</a>";
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setSubject("Boardly - Email Verification");
            helper.setFrom(fromEmail);
            System.out.println("Email sent to " + to + " with link: " + link);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String link = frontendUrl + "/reset-password?token=" + token;
            String emailContent = "<h1>Password Reset</h1><p>Click the link below to reset your password:</p><a href=\"" + link + "\">Reset Password</a>";
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setSubject("Boardly - Password Reset");
            helper.setFrom(fromEmail);
            System.out.println("Email sent to " + to + " with link: " + link);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}

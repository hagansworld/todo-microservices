//package com.todo.user_service.service;
//
//
//import com.todo.user_service.exception.EmailSendFailedException;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//
//    /* ===================== SEND VERIFICATION EMAIL ===================== */
//    public void sendVerificationEmail(String to, String subject, String htmlContent) {
//        try {
//            log.info("Preparing to send verification code to {}", to);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            // Set the 'From' email to show only "Medicare Health" as the sender
//            helper.setFrom("Medicare Health <hagan2842@gmail.com>");
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("Verification email sent successfully to {}", to);
//
//        } catch (MessagingException e) {
//            log.error("Failed to send verification email to {}", to, e);
//            throw new EmailSendFailedException("Failed to send email verification to " + to);
//        }
//    }
//
//
//}

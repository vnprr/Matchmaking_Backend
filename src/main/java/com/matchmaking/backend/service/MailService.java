package com.matchmaking.backend.service;

import com.matchmaking.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Click to verify: http://localhost:8080/api/auth/verify?code=" + user.getVerificationCode());
        mailSender.send(message);
    }
}


//
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class MailService {
//
//    private final JavaMailSender mailSender;
//
//    public void sendVerificationEmail(String to, String verificationCode) {
//        String subject = "Potwierdź rejestrację konta";
//        String verificationLink = "http://localhost:8080/api/auth/verify?code=" + verificationCode;
//
//        String text = "Dziękujemy za rejestrację. Kliknij w link, aby aktywować konto:\n" + verificationLink;
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        mailSender.send(message);
//    }
//}
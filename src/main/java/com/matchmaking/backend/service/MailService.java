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

    public void sendPasswordResetEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset hasła");
        message.setText("Aby zresetować hasło, kliknij w link: http://localhost:5173/reset-password?token="
                + user.getVerificationCode());
        mailSender.send(message);
    }

    public void sendEmailChangeNotification(String oldEmail, String newEmail) {
        // Powiadomienie na stary adres
        SimpleMailMessage oldEmailMessage = new SimpleMailMessage();
        oldEmailMessage.setTo(oldEmail);
        oldEmailMessage.setSubject("Zmiana adresu email w aplikacji");
        oldEmailMessage.setText("Twój adres email został zmieniony na: " + newEmail +
                ". Jeśli to nie Ty dokonałeś tej zmiany, skontaktuj się z administratorem.");
        mailSender.send(oldEmailMessage);

        // Potwierdzenie na nowy adres
        SimpleMailMessage newEmailMessage = new SimpleMailMessage();
        newEmailMessage.setTo(newEmail);
        newEmailMessage.setSubject("Potwierdzenie zmiany adresu email");
        newEmailMessage.setText("Ten adres email został ustawiony jako Twój nowy adres kontaktowy w aplikacji.");
        mailSender.send(newEmailMessage);
    }
}

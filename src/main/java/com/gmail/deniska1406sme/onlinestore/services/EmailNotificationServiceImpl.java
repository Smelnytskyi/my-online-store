package com.gmail.deniska1406sme.onlinestore.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private JavaMailSender mailSender;

    @Override
    public void sendOrderConfirmationEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("deniska1406sme@gmail.com");

        mailSender.send(message);
    }
}

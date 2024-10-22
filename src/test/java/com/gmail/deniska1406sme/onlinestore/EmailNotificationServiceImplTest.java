package com.gmail.deniska1406sme.onlinestore;

import com.gmail.deniska1406sme.onlinestore.services.EmailNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailNotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationServiceImpl emailNotificationService;

    @Test
    public void testSendOrderConfirmationEmail() {
        String to = "recipient@example.com";
        String subject = "Order Confirmation";
        String text = "Thank you for your order!";

        emailNotificationService.sendOrderConfirmationEmail(to, subject, text);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("deniska1406sme@gmail.com");

        verify(mailSender).send(message);
    }
}

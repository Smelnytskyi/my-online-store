package com.gmail.deniska1406sme.onlinestore.services;

public interface EmailNotificationService {

    void sendOrderConfirmationEmail(String to, String subject, String text);
}

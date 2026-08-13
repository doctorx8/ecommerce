package com.ecommerce.store.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String storeName;
    private final String publicUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:true}") boolean enabled,
            @Value("${app.mail.from:noreply@karwan.local}") String from,
            @Value("${app.store.name:Karwan}") String storeName,
            @Value("${app.store.public-url:http://localhost:8080}") String publicUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.storeName = storeName;
        this.publicUrl = publicUrl;
    }

    public void sendOrderConfirmation(String to, String orderNumber, String total) {
        send(to,
                storeName + " order confirmation " + orderNumber,
                """
                Thanks for your order at %s.

                Order: %s
                Total: %s

                View your account: %s/account
                """.formatted(storeName, orderNumber, total, publicUrl));
    }

    public void sendShippingUpdate(String to, String orderNumber, String status, String comment) {
        send(to,
                storeName + " shipping update " + orderNumber,
                """
                Your order %s is now: %s

                %s

                Track in your account: %s/account
                """.formatted(orderNumber, status, comment != null ? comment : "", publicUrl));
    }

    public void sendPasswordReset(String to, String token) {
        String link = publicUrl + "/auth?resetToken=" + token;
        send(to,
                storeName + " password reset",
                """
                Reset your %s password using this link (valid 1 hour):

                %s

                If you did not request this, ignore this email.
                """.formatted(storeName, link));
    }

    private void send(String to, String subject, String body) {
        if (!enabled) {
            log.info("Mail disabled; skip to={} subject={}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Mail sent to={} subject={}", to, subject);
        } catch (Exception ex) {
            log.warn("Mail failed to={} subject={}: {}", to, subject, ex.getMessage());
        }
    }
}

package com.marketplace.notification.domain.port;

public interface EmailSender {
    void sendEmail(String to, String subject, String body);
}

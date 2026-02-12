package com.marketplace.notification.application.model;

public record EmailMessage(String subject, String textBody, String htmlBody) {
    public String body() {
        return textBody;
    }
}

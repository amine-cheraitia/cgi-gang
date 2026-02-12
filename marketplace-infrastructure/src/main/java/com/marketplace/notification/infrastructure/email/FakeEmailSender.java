package com.marketplace.notification.infrastructure.email;

import com.marketplace.notification.domain.port.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile({"dev", "test"})
public class FakeEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(FakeEmailSender.class);
    private final List<EmailRecord> sentEmails = new CopyOnWriteArrayList<>();

    @Override
    public void sendEmail(String to, String subject, String textBody, String htmlBody) {
        sentEmails.add(new EmailRecord(to, subject, textBody, htmlBody));
        log.info("Fake email sent to={}, subject={}, textBody={}", to, subject, textBody);
    }

    public List<EmailRecord> sentEmails() {
        return List.copyOf(sentEmails);
    }

    public void clear() {
        sentEmails.clear();
    }

    public record EmailRecord(String to, String subject, String textBody, String htmlBody) {
        public String body() {
            return textBody;
        }
    }
}

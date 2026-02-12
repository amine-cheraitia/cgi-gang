package com.marketplace.notification.infrastructure.email;

import com.marketplace.notification.domain.port.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Profile("!dev & !test")
public class BrevoEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender-email:noreply@marketplace.local}")
    private String senderEmail;

    @Value("${brevo.sender-name:Marketplace}")
    private String senderName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Brevo API key absente, email non envoye. to={}, subject={}", to, subject);
            return;
        }

        String payload = """
            {
              "sender": {"name": "%s", "email": "%s"},
              "to": [{"email": "%s"}],
              "subject": "%s",
              "textContent": "%s"
            }
            """.formatted(
            escape(senderName),
            escape(senderEmail),
            escape(to),
            escape(subject),
            escape(body)
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
            .header("accept", "application/json")
            .header("api-key", apiKey)
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("Brevo reponse en erreur status={}, body={}", response.statusCode(), response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Erreur envoi Brevo", e);
        } catch (IOException e) {
            log.warn("Erreur envoi Brevo", e);
        }
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}

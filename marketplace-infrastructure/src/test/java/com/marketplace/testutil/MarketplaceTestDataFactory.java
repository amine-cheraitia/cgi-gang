package com.marketplace.testutil;

import java.util.Locale;

public final class MarketplaceTestDataFactory {

    private MarketplaceTestDataFactory() {
    }

    public static String listingPayload(String eventId, double price, String currency) {
        return String.format(Locale.US, """
            {
              "eventId":"%s",
              "price":%.2f,
              "currency":"%s"
            }
            """, eventId, price, currency);
    }

    public static String orderPayload(String listingId) {
        return """
            {
              "listingId":"%s"
            }
            """.formatted(listingId);
    }

    public static String waitlistPayload(String eventId) {
        return """
            {
              "eventId":"%s"
            }
            """.formatted(eventId);
    }

    public static String presignPayload(String filename, String contentType) {
        return """
            {
              "filename":"%s",
              "contentType":"%s"
            }
            """.formatted(filename, contentType);
    }

    public static String paymentWebhookPayload(String orderId, String status, String providerTransactionId) {
        String transactionPart = providerTransactionId == null
            ? ""
            : """
              ,
              "providerTransactionId":"%s"
            """.formatted(providerTransactionId);

        return """
            {
              "orderId":"%s",
              "status":"%s"%s
            }
            """.formatted(orderId, status, transactionPart);
    }
}

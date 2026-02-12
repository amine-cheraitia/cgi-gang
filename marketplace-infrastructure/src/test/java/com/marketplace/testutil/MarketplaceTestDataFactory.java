package com.marketplace.testutil;

import java.util.Locale;

public final class MarketplaceTestDataFactory {

    private MarketplaceTestDataFactory() {
    }

    public static String listingPayload(String eventId, String sellerId, double price, String currency) {
        return String.format(Locale.US, """
            {
              "eventId":"%s",
              "sellerId":"%s",
              "price":%.2f,
              "currency":"%s"
            }
            """, eventId, sellerId, price, currency);
    }

    public static String orderPayload(String listingId, String buyerId) {
        return """
            {
              "listingId":"%s",
              "buyerId":"%s"
            }
            """.formatted(listingId, buyerId);
    }

    public static String waitlistPayload(String eventId, String userId) {
        return """
            {
              "eventId":"%s",
              "userId":"%s"
            }
            """.formatted(eventId, userId);
    }

    public static String presignPayload(String sellerId, String filename, String contentType) {
        return """
            {
              "sellerId":"%s",
              "filename":"%s",
              "contentType":"%s"
            }
            """.formatted(sellerId, filename, contentType);
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

package com.marketplace.shared.domain.exception;

public enum ErrorCode {
    VALIDATION_ERROR("GEN-001", 400, "Requete invalide"),
    RESOURCE_NOT_FOUND("GEN-404", 404, "Ressource introuvable"),
    BUSINESS_CONFLICT("GEN-409", 409, "Conflit metier"),
    INTERNAL_ERROR("GEN-999", 500, "Erreur interne"),

    AUTH_REQUIRED("AUTH-001", 401, "Authentification requise"),
    AUTH_BAD_CREDENTIALS("AUTH-002", 401, "Identifiants invalides"),
    ACCESS_DENIED("AUTH-003", 403, "Acces refuse"),

    LISTING_NOT_FOUND("LST-001", 404, "Listing introuvable"),
    LISTING_NOT_CERTIFIED("LST-002", 409, "Listing non certifie"),
    LISTING_INVALID_STATE("LST-003", 409, "Etat de listing invalide"),
    ORDER_NOT_FOUND("ORD-001", 404, "Commande introuvable"),
    ORDER_ALREADY_PAID("ORD-002", 409, "Commande deja payee"),
    ORDER_INVALID_STATE("ORD-003", 409, "Etat de commande invalide"),
    EVENT_NOT_FOUND("CAT-001", 404, "Evenement introuvable"),
    CATALOG_PROVIDER_UNAVAILABLE("CAT-002", 503, "Catalogue indisponible"),
    NOTIFICATION_TEMPLATE_PAYLOAD_INVALID("NTF-001", 400, "Payload notification invalide"),
    PAYMENT_PROVIDER_ERROR("PAY-001", 502, "Erreur du provider de paiement"),
    PAYMENT_WEBHOOK_INVALID("PAY-002", 400, "Webhook paiement invalide"),
    WAITLIST_SUBSCRIPTION_NOT_FOUND("WAI-001", 404, "Inscription waitlist introuvable"),
    WAITLIST_ALREADY_SUBSCRIBED("WAI-002", 409, "Deja inscrit a la waitlist"),
    USER_NOT_FOUND("USR-001", 404, "Utilisateur introuvable");

    private final String code;
    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}

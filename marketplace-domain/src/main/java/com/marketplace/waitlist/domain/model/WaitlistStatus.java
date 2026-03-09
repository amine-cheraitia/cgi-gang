package com.marketplace.waitlist.domain.model;

public enum WaitlistStatus {
    WAITING,    // inscrit, en attente de notification
    NOTIFIED,   // notifié, fenêtre de 15 min pour acheter
    EXPIRED,    // fenêtre expirée, passé au suivant
    PURCHASED   // a acheté le billet
}

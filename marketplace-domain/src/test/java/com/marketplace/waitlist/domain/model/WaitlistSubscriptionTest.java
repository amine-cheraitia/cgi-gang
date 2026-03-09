package com.marketplace.waitlist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitlistSubscriptionTest {

    @Test
    void createShouldGenerateSubscriptionWithWaitingStatus() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");

        assertThat(subscription.getId()).isNotBlank();
        assertThat(subscription.getEventId()).isEqualTo("evt-1");
        assertThat(subscription.getUserId()).isEqualTo("buyer-1");
        assertThat(subscription.getCreatedAt()).isNotNull();
        assertThat(subscription.getStatus()).isEqualTo(WaitlistStatus.WAITING);
        assertThat(subscription.getNotifiedAt()).isNull();
    }

    @Test
    void rehydrateShouldKeepPersistedValues() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-10T10:15:30Z");

        WaitlistSubscription subscription = WaitlistSubscription.rehydrate(
            "wai-1", "evt-1", "buyer-1", createdAt, WaitlistStatus.WAITING, null
        );

        assertThat(subscription.getId()).isEqualTo("wai-1");
        assertThat(subscription.getEventId()).isEqualTo("evt-1");
        assertThat(subscription.getUserId()).isEqualTo("buyer-1");
        assertThat(subscription.getCreatedAt()).isEqualTo(createdAt);
        assertThat(subscription.getStatus()).isEqualTo(WaitlistStatus.WAITING);
    }

    @Test
    void markNotifiedShouldTransitionToNotifiedStatus() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");

        subscription.markNotified();

        assertThat(subscription.getStatus()).isEqualTo(WaitlistStatus.NOTIFIED);
        assertThat(subscription.getNotifiedAt()).isNotNull();
    }

    @Test
    void markNotifiedShouldFailIfAlreadyNotified() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");
        subscription.markNotified();

        assertThatThrownBy(subscription::markNotified)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expireShouldTransitionToExpiredStatus() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");
        subscription.markNotified();

        subscription.expire();

        assertThat(subscription.getStatus()).isEqualTo(WaitlistStatus.EXPIRED);
    }

    @Test
    void expireShouldFailIfNotNotified() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");

        assertThatThrownBy(subscription::expire)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isExpiredShouldReturnTrueWhenWindowElapsed() {
        OffsetDateTime pastNotifiedAt = OffsetDateTime.now().minusMinutes(20);
        WaitlistSubscription subscription = WaitlistSubscription.rehydrate(
            "wai-1", "evt-1", "buyer-1", OffsetDateTime.now().minusHours(1),
            WaitlistStatus.NOTIFIED, pastNotifiedAt
        );

        assertThat(subscription.isExpired(15)).isTrue();
    }

    @Test
    void isExpiredShouldReturnFalseWhenWindowNotElapsed() {
        OffsetDateTime recentNotifiedAt = OffsetDateTime.now().minusMinutes(5);
        WaitlistSubscription subscription = WaitlistSubscription.rehydrate(
            "wai-1", "evt-1", "buyer-1", OffsetDateTime.now().minusHours(1),
            WaitlistStatus.NOTIFIED, recentNotifiedAt
        );

        assertThat(subscription.isExpired(15)).isFalse();
    }
}

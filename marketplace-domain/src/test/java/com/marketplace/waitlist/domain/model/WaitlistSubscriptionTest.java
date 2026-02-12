package com.marketplace.waitlist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WaitlistSubscriptionTest {

    @Test
    void createShouldGenerateSubscriptionWithMetadata() {
        WaitlistSubscription subscription = WaitlistSubscription.create("evt-1", "buyer-1");

        assertThat(subscription.getId()).isNotBlank();
        assertThat(subscription.getEventId()).isEqualTo("evt-1");
        assertThat(subscription.getUserId()).isEqualTo("buyer-1");
        assertThat(subscription.getCreatedAt()).isNotNull();
    }

    @Test
    void rehydrateShouldKeepPersistedValues() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-10T10:15:30Z");

        WaitlistSubscription subscription = WaitlistSubscription.rehydrate("wai-1", "evt-1", "buyer-1", createdAt);

        assertThat(subscription.getId()).isEqualTo("wai-1");
        assertThat(subscription.getEventId()).isEqualTo("evt-1");
        assertThat(subscription.getUserId()).isEqualTo("buyer-1");
        assertThat(subscription.getCreatedAt()).isEqualTo(createdAt);
    }
}

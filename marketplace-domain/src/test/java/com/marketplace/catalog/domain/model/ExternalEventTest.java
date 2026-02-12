package com.marketplace.catalog.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalEventTest {

    @Test
    void shouldCreateEventWithValidFields() {
        ExternalEvent event = new ExternalEvent("evt-1", "Concert", Instant.now(), "Arena", "Paris");

        assertThat(event.id()).isEqualTo("evt-1");
        assertThat(event.name()).isEqualTo("Concert");
    }

    @Test
    void shouldRejectInvalidMandatoryFields() {
        assertThatThrownBy(() -> new ExternalEvent("", "Concert", Instant.now(), "Arena", "Paris"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Event id is required");
        assertThatThrownBy(() -> new ExternalEvent("evt-1", " ", Instant.now(), "Arena", "Paris"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Event name is required");
    }
}

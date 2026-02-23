package com.marketplace.catalog.infrastructure.provider;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "catalog.provider", havingValue = "mock", matchIfMissing = true)
public class MockCatalogProvider implements CatalogProvider {
    private final List<ExternalEvent> events = List.of(
        new ExternalEvent("evt_taylor_paris", "Taylor Swift - The Eras Tour", Instant.parse("2026-06-15T20:00:00Z"), "Stade de France", "Paris"),
        new ExternalEvent("evt_psg_om", "PSG vs OM", Instant.parse("2026-03-20T21:00:00Z"), "Parc des Princes", "Paris"),
        new ExternalEvent("evt_coldplay_nanterre", "Coldplay - Music Of The Spheres", Instant.parse("2026-07-10T19:30:00Z"), "U Arena", "Nanterre"),
        new ExternalEvent("evt_burna_dakar", "Burna Boy Live", Instant.parse("2026-09-05T20:30:00Z"), "Dakar Arena", "Dakar")
    );

    @Override
    public List<ExternalEvent> searchEvents(String query) {
        if ("__FAIL__".equalsIgnoreCase(query)) {
            throw new IllegalStateException("Mock catalog provider unavailable");
        }
        if (query == null || query.isBlank()) {
            return events;
        }
        String q = query.toLowerCase(Locale.ROOT);
        return events.stream()
            .filter(e -> e.name().toLowerCase(Locale.ROOT).contains(q)
                || e.city().toLowerCase(Locale.ROOT).contains(q)
                || e.venue().toLowerCase(Locale.ROOT).contains(q))
            .toList();
    }

    @Override
    public Optional<ExternalEvent> getEventById(String eventId) {
        if ("__FAIL__".equalsIgnoreCase(eventId)) {
            throw new IllegalStateException("Mock catalog provider unavailable");
        }
        return events.stream().filter(e -> e.id().equals(eventId)).findFirst();
    }
}

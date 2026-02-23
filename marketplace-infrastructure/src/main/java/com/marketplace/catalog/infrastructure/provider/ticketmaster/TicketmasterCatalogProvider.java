package com.marketplace.catalog.infrastructure.provider.ticketmaster;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.catalog.infrastructure.provider.ticketmaster.dto.TicketmasterEventDto;
import com.marketplace.catalog.infrastructure.provider.ticketmaster.dto.TicketmasterSearchResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "catalog.provider", havingValue = "ticketmaster")
public class TicketmasterCatalogProvider implements CatalogProvider {

    private static final Logger log = LoggerFactory.getLogger(TicketmasterCatalogProvider.class);

    private final RestClient restClient;
    private final String apiKey;

    public TicketmasterCatalogProvider(
            @Value("${ticketmaster.base-url}") String baseUrl,
            @Value("${ticketmaster.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<ExternalEvent> searchEvents(String query) {
        try {
            TicketmasterSearchResponseDto response = restClient.get()
                    .uri(u -> u.path("/events")
                            .queryParam("keyword", query)
                            .queryParam("apikey", apiKey)
                            .queryParam("locale", "*")
                            .queryParam("size", 20)
                            .build())
                    .retrieve()
                    .body(TicketmasterSearchResponseDto.class);

            if (response == null || response.embedded() == null || response.embedded().events() == null) {
                return List.of();
            }
            return response.embedded().events().stream()
                    .map(this::toExternalEvent)
                    .toList();
        } catch (RestClientException e) {
            log.error("Ticketmaster API error during searchEvents(query={}): {}", query, e.getMessage());
            throw new IllegalStateException("Ticketmaster catalog provider unavailable", e);
        }
    }

    @Override
    public Optional<ExternalEvent> getEventById(String eventId) {
        try {
            TicketmasterEventDto event = restClient.get()
                    .uri(u -> u.path("/events/{id}")
                            .queryParam("apikey", apiKey)
                            .queryParam("locale", "*")
                            .build(eventId))
                    .retrieve()
                    .body(TicketmasterEventDto.class);

            return Optional.ofNullable(event).map(this::toExternalEvent);
        } catch (RestClientException e) {
            log.error("Ticketmaster API error during getEventById(id={}): {}", eventId, e.getMessage());
            throw new IllegalStateException("Ticketmaster catalog provider unavailable", e);
        }
    }

    private ExternalEvent toExternalEvent(TicketmasterEventDto dto) {
        String venue = "";
        String city = "";

        if (dto.embedded() != null && dto.embedded().venues() != null && !dto.embedded().venues().isEmpty()) {
            TicketmasterEventDto.EventEmbedded.VenueDto v = dto.embedded().venues().get(0);
            venue = v.name() != null ? v.name() : "";
            city = (v.city() != null && v.city().name() != null) ? v.city().name() : "";
        }

        Instant date = Instant.EPOCH;
        if (dto.dates() != null && dto.dates().start() != null) {
            String dateTime = dto.dates().start().dateTime();
            if (dateTime != null && !dateTime.isBlank()) {
                date = Instant.parse(dateTime);
            } else if (dto.dates().start().localDate() != null) {
                date = Instant.parse(dto.dates().start().localDate() + "T00:00:00Z");
            }
        }

        return new ExternalEvent(dto.id(), dto.name(), date, venue, city);
    }
}

package com.marketplace.catalog.infrastructure.rest;

import com.marketplace.catalog.application.usecase.GetEventByIdUseCase;
import com.marketplace.catalog.application.usecase.SearchEventsUseCase;
import com.marketplace.catalog.infrastructure.rest.dto.EventResponse;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Catalog", description = "Consultation du catalogue evenementiel")
public class EventController {
    private final SearchEventsUseCase searchEventsUseCase;
    private final GetEventByIdUseCase getEventByIdUseCase;
    private final ListingRepository listingRepository;

    public EventController(SearchEventsUseCase searchEventsUseCase,
                           GetEventByIdUseCase getEventByIdUseCase,
                           ListingRepository listingRepository) {
        this.searchEventsUseCase = searchEventsUseCase;
        this.getEventByIdUseCase = getEventByIdUseCase;
        this.listingRepository = listingRepository;
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des evenements", description = "Retourne les evenements du catalogue filtres par query.")
    public List<EventResponse> search(@RequestParam(name = "query", required = false) String query) {
        return searchEventsUseCase.execute(query).stream()
            .map(event -> {
                int available = listingRepository.countCertifiedByEvent(new ExternalEventId(event.id()));
                return EventResponse.from(event, available);
            })
            .toList();
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Recuperer un evenement par id", description = "Retourne le detail d'un evenement catalogue.")
    public EventResponse getById(@PathVariable String eventId) {
        var event = getEventByIdUseCase.execute(eventId);
        int available = listingRepository.countCertifiedByEvent(new ExternalEventId(event.id()));
        return EventResponse.from(event, available);
    }
}

package com.marketplace.catalog.domain.port;

import com.marketplace.catalog.domain.model.ExternalEvent;

import java.util.List;
import java.util.Optional;

public interface CatalogProvider {
    List<ExternalEvent> searchEvents(String query);

    Optional<ExternalEvent> getEventById(String eventId);
}

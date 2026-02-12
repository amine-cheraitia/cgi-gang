package com.marketplace.catalog.application.usecase;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchEventsUseCase {
    private final CatalogProvider catalogProvider;

    public SearchEventsUseCase(CatalogProvider catalogProvider) {
        this.catalogProvider = catalogProvider;
    }

    public List<ExternalEvent> execute(String query) {
        return catalogProvider.searchEvents(query == null ? "" : query);
    }
}

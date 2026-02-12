package com.marketplace.catalog.application.usecase;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchEventsUseCase {
    private final CatalogProvider catalogProvider;

    public SearchEventsUseCase(CatalogProvider catalogProvider) {
        this.catalogProvider = catalogProvider;
    }

    public List<ExternalEvent> execute(String query) {
        try {
            return catalogProvider.searchEvents(query == null ? "" : query);
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.CATALOG_PROVIDER_UNAVAILABLE);
        }
    }
}

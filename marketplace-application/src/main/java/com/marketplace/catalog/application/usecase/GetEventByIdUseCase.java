package com.marketplace.catalog.application.usecase;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class GetEventByIdUseCase {
    private final CatalogProvider catalogProvider;

    public GetEventByIdUseCase(CatalogProvider catalogProvider) {
        this.catalogProvider = catalogProvider;
    }

    public ExternalEvent execute(String eventId) {
        return catalogProvider.getEventById(eventId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }
}

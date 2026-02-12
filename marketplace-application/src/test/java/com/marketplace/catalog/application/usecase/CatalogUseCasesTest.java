package com.marketplace.catalog.application.usecase;

import com.marketplace.catalog.domain.model.ExternalEvent;
import com.marketplace.catalog.domain.port.CatalogProvider;
import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogUseCasesTest {

    @Mock
    private CatalogProvider catalogProvider;
    @InjectMocks
    private SearchEventsUseCase searchEventsUseCase;
    @InjectMocks
    private GetEventByIdUseCase getEventByIdUseCase;

    @Test
    void searchShouldDefaultToEmptyQueryAndReturnEvents() {
        ExternalEvent event = new ExternalEvent("evt-1", "Concert", Instant.now(), "Stade", "Paris");
        when(catalogProvider.searchEvents("")).thenReturn(List.of(event));

        List<ExternalEvent> result = searchEventsUseCase.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Concert");
    }

    @Test
    void searchShouldMapProviderFailureToCat002() {
        when(catalogProvider.searchEvents("q")).thenThrow(new IllegalStateException("down"));

        assertThatThrownBy(() -> searchEventsUseCase.execute("q"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.CATALOG_PROVIDER_UNAVAILABLE);
    }

    @Test
    void getByIdShouldReturnEventAndMapFailures() {
        ExternalEvent event = new ExternalEvent("evt-1", "Match", Instant.now(), "Arena", "Lyon");
        when(catalogProvider.getEventById("evt-1")).thenReturn(Optional.of(event));
        when(catalogProvider.getEventById("missing")).thenReturn(Optional.empty());
        when(catalogProvider.getEventById("boom")).thenThrow(new RuntimeException("down"));

        assertThat(getEventByIdUseCase.execute("evt-1").name()).isEqualTo("Match");
        assertThatThrownBy(() -> getEventByIdUseCase.execute("missing"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        assertThatThrownBy(() -> getEventByIdUseCase.execute("boom"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.CATALOG_PROVIDER_UNAVAILABLE);
    }
}

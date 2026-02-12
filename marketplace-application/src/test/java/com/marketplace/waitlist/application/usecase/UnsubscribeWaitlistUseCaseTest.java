package com.marketplace.waitlist.application.usecase;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnsubscribeWaitlistUseCaseTest {

    @Mock
    private WaitlistSubscriptionRepository repository;

    @InjectMocks
    private UnsubscribeWaitlistUseCase useCase;

    @Test
    void shouldDeleteSubscriptionWhenExists() {
        when(repository.deleteByEventIdAndUserId("evt_1", "buyer_1")).thenReturn(true);

        assertThatCode(() -> useCase.execute("evt_1", "buyer_1")).doesNotThrowAnyException();
    }

    @Test
    void shouldReturnWai001WhenSubscriptionMissing() {
        when(repository.deleteByEventIdAndUserId("evt_1", "buyer_1")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("evt_1", "buyer_1"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.WAITLIST_SUBSCRIPTION_NOT_FOUND);
    }
}

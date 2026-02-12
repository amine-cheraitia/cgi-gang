package com.marketplace.waitlist.application.usecase;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeWaitlistUseCaseTest {

    @Mock
    private WaitlistSubscriptionRepository repository;

    @InjectMocks
    private SubscribeWaitlistUseCase useCase;

    @Test
    void shouldCreateSubscription() {
        when(repository.existsByEventIdAndUserId("evt_1", "buyer_1")).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any(WaitlistSubscription.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        WaitlistSubscription created = useCase.execute("evt_1", "buyer_1");

        assertThat(created.getEventId()).isEqualTo("evt_1");
        assertThat(created.getUserId()).isEqualTo("buyer_1");
    }

    @Test
    void shouldRejectDuplicateSubscription() {
        when(repository.existsByEventIdAndUserId("evt_1", "buyer_1")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("evt_1", "buyer_1"))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.WAITLIST_ALREADY_SUBSCRIBED);
    }
}

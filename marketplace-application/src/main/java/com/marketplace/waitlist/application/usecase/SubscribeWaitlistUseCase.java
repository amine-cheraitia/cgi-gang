package com.marketplace.waitlist.application.usecase;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscribeWaitlistUseCase {
    private final WaitlistSubscriptionRepository repository;

    public SubscribeWaitlistUseCase(WaitlistSubscriptionRepository repository) {
        this.repository = repository;
    }

    public WaitlistSubscription execute(String eventId, String userId) {
        if (repository.existsByEventIdAndUserId(eventId, userId)) {
            throw new BusinessException(ErrorCode.WAITLIST_ALREADY_SUBSCRIBED);
        }
        return repository.save(WaitlistSubscription.create(eventId, userId));
    }
}

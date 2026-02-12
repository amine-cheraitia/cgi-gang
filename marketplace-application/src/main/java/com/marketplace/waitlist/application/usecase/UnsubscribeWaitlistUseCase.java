package com.marketplace.waitlist.application.usecase;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class UnsubscribeWaitlistUseCase {
    private final WaitlistSubscriptionRepository repository;

    public UnsubscribeWaitlistUseCase(WaitlistSubscriptionRepository repository) {
        this.repository = repository;
    }

    public void execute(String eventId, String userId) {
        boolean deleted = repository.deleteByEventIdAndUserId(eventId, userId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.WAITLIST_SUBSCRIPTION_NOT_FOUND);
        }
    }
}

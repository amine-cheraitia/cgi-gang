package com.marketplace.waitlist.infrastructure.persistence;

import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaWaitlistSubscriptionRepositoryAdapter implements WaitlistSubscriptionRepository {
    private final SpringDataWaitlistSubscriptionRepository repository;

    public JpaWaitlistSubscriptionRepositoryAdapter(SpringDataWaitlistSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public WaitlistSubscription save(WaitlistSubscription subscription) {
        WaitlistSubscriptionEntity entity = new WaitlistSubscriptionEntity();
        entity.setId(subscription.getId());
        entity.setEventId(subscription.getEventId());
        entity.setUserId(subscription.getUserId());
        entity.setCreatedAt(subscription.getCreatedAt());
        WaitlistSubscriptionEntity saved = repository.save(entity);
        return WaitlistSubscription.rehydrate(saved.getId(), saved.getEventId(), saved.getUserId(), saved.getCreatedAt());
    }

    @Override
    public boolean existsByEventIdAndUserId(String eventId, String userId) {
        return repository.existsByEventIdAndUserId(eventId, userId);
    }

    @Override
    public List<WaitlistSubscription> findByEventId(String eventId) {
        return repository.findByEventId(eventId).stream()
            .map(entity -> WaitlistSubscription.rehydrate(
                entity.getId(),
                entity.getEventId(),
                entity.getUserId(),
                entity.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public boolean deleteByEventIdAndUserId(String eventId, String userId) {
        return repository.deleteByEventIdAndUserId(eventId, userId) > 0;
    }
}

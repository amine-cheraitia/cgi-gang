package com.marketplace.waitlist.infrastructure.persistence;

import com.marketplace.waitlist.domain.model.WaitlistStatus;
import com.marketplace.waitlist.domain.model.WaitlistSubscription;
import com.marketplace.waitlist.domain.repository.WaitlistSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JpaWaitlistSubscriptionRepositoryAdapter implements WaitlistSubscriptionRepository {

    private final SpringDataWaitlistSubscriptionRepository repository;

    public JpaWaitlistSubscriptionRepositoryAdapter(SpringDataWaitlistSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public WaitlistSubscription save(WaitlistSubscription subscription) {
        WaitlistSubscriptionEntity entity = toEntity(subscription);
        WaitlistSubscriptionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByEventIdAndUserId(String eventId, String userId) {
        return repository.existsByEventIdAndUserId(eventId, userId);
    }

    @Override
    public List<WaitlistSubscription> findByEventId(String eventId) {
        return repository.findByEventId(eventId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<WaitlistSubscription> findByEventIdAndStatus(String eventId, WaitlistStatus status) {
        return repository.findByEventIdAndStatus(eventId, status).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean deleteByEventIdAndUserId(String eventId, String userId) {
        return repository.deleteByEventIdAndUserId(eventId, userId) > 0;
    }

    @Override
    public Optional<WaitlistSubscription> findFirstWaitingByEventId(String eventId) {
        return repository.findFirstByEventIdAndStatusOrderByCreatedAtAsc(eventId, WaitlistStatus.WAITING)
            .map(this::toDomain);
    }

    @Override
    public List<WaitlistSubscription> findExpiredNotifications(int windowMinutes) {
        OffsetDateTime expiryThreshold = OffsetDateTime.now().minusMinutes(windowMinutes);
        return repository.findExpiredNotifications(expiryThreshold).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public Optional<WaitlistSubscription> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    private WaitlistSubscriptionEntity toEntity(WaitlistSubscription sub) {
        WaitlistSubscriptionEntity entity = new WaitlistSubscriptionEntity();
        entity.setId(sub.getId());
        entity.setEventId(sub.getEventId());
        entity.setUserId(sub.getUserId());
        entity.setCreatedAt(sub.getCreatedAt());
        entity.setStatus(sub.getStatus() != null ? sub.getStatus() : WaitlistStatus.WAITING);
        entity.setNotifiedAt(sub.getNotifiedAt());
        return entity;
    }

    private WaitlistSubscription toDomain(WaitlistSubscriptionEntity entity) {
        return WaitlistSubscription.rehydrate(
            entity.getId(),
            entity.getEventId(),
            entity.getUserId(),
            entity.getCreatedAt(),
            entity.getStatus() != null ? entity.getStatus() : WaitlistStatus.WAITING,
            entity.getNotifiedAt()
        );
    }
}

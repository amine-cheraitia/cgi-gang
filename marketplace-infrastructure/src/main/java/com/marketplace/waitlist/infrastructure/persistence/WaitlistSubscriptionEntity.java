package com.marketplace.waitlist.infrastructure.persistence;

import com.marketplace.waitlist.domain.model.WaitlistStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "waitlist_subscriptions")
public class WaitlistSubscriptionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 120)
    private String eventId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaitlistStatus status;

    @Column(name = "notified_at")
    private OffsetDateTime notifiedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public WaitlistStatus getStatus() { return status; }
    public void setStatus(WaitlistStatus status) { this.status = status; }

    public OffsetDateTime getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(OffsetDateTime notifiedAt) { this.notifiedAt = notifiedAt; }
}

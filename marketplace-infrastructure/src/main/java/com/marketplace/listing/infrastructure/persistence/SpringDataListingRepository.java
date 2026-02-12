package com.marketplace.listing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataListingRepository extends JpaRepository<ListingEntity, String> {
    int countByEventIdAndStatus(String eventId, String status);

    Optional<ListingEntity> findFirstByEventIdAndStatusOrderByPriceAsc(String eventId, String status);

    Optional<ListingEntity> findFirstByEventIdAndStatusOrderByPriceDesc(String eventId, String status);

    List<ListingEntity> findAllByEventIdAndStatus(String eventId, String status);

    List<ListingEntity> findAllByStatus(String status);
}

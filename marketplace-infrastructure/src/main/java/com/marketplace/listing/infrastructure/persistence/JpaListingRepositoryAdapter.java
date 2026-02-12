package com.marketplace.listing.infrastructure.persistence;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.model.ListingStatus;
import com.marketplace.listing.domain.repository.ListingRepository;
import com.marketplace.listing.domain.valueobject.ExternalEventId;
import com.marketplace.shared.domain.valueobject.Money;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaListingRepositoryAdapter implements ListingRepository {
    private final SpringDataListingRepository repository;

    public JpaListingRepositoryAdapter(SpringDataListingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Listing save(Listing listing) {
        ListingEntity entity = toEntity(listing);
        ListingEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Listing> findById(String listingId) {
        return repository.findById(listingId).map(this::toDomain);
    }

    @Override
    public int countCertifiedByEvent(ExternalEventId eventId) {
        return repository.countByEventIdAndStatus(eventId.value(), ListingStatus.CERTIFIED.name());
    }

    @Override
    public Optional<Listing> findCheapestCertifiedByEvent(ExternalEventId eventId) {
        return repository.findFirstByEventIdAndStatusOrderByPriceAsc(eventId.value(), ListingStatus.CERTIFIED.name())
            .map(this::toDomain);
    }

    @Override
    public Optional<Listing> findMostExpensiveCertifiedByEvent(ExternalEventId eventId) {
        return repository.findFirstByEventIdAndStatusOrderByPriceDesc(eventId.value(), ListingStatus.CERTIFIED.name())
            .map(this::toDomain);
    }

    @Override
    public List<Listing> findAllCertifiedByEvent(ExternalEventId eventId) {
        return repository.findAllByEventIdAndStatus(eventId.value(), ListingStatus.CERTIFIED.name())
            .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Listing> findAllCertified() {
        return repository.findAllByStatus(ListingStatus.CERTIFIED.name()).stream().map(this::toDomain).toList();
    }

    private ListingEntity toEntity(Listing listing) {
        ListingEntity entity = new ListingEntity();
        entity.setId(listing.getId());
        entity.setEventId(listing.getExternalEventId().value());
        entity.setSellerId(listing.getSellerId());
        entity.setPrice(listing.getPrice().amount());
        entity.setCurrency(listing.getPrice().currency().getCurrencyCode());
        entity.setStatus(listing.getStatus().name());
        return entity;
    }

    private Listing toDomain(ListingEntity entity) {
        return Listing.rehydrate(
            entity.getId(),
            new ExternalEventId(entity.getEventId()),
            entity.getSellerId(),
            Money.of(entity.getPrice(), Currency.getInstance(entity.getCurrency())),
            ListingStatus.valueOf(entity.getStatus())
        );
    }
}

package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPendingListingsUseCase {
    private final ListingRepository listingRepository;

    public ListPendingListingsUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<Listing> execute() {
        return listingRepository.findAllPendingCertification();
    }
}

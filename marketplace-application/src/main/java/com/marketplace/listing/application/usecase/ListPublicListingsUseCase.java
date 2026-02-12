package com.marketplace.listing.application.usecase;

import com.marketplace.listing.domain.model.Listing;
import com.marketplace.listing.domain.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPublicListingsUseCase {
    private final ListingRepository listingRepository;

    public ListPublicListingsUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public List<Listing> execute() {
        return listingRepository.findAllCertified();
    }
}

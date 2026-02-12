package com.marketplace.listing.infrastructure.rest;

import com.marketplace.listing.application.usecase.CertifyListingUseCase;
import com.marketplace.listing.infrastructure.rest.dto.ListingResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certification")
public class CertificationController {
    private final CertifyListingUseCase certifyListingUseCase;

    public CertificationController(CertifyListingUseCase certifyListingUseCase) {
        this.certifyListingUseCase = certifyListingUseCase;
    }

    @PostMapping("/{listingId}/certify")
    public ListingResponse certify(@PathVariable String listingId) {
        return ListingResponse.from(certifyListingUseCase.execute(listingId));
    }
}

package com.marketplace.listing.infrastructure.rest;

import com.marketplace.listing.application.usecase.CertifyListingUseCase;
import com.marketplace.listing.application.usecase.ListPendingListingsUseCase;
import com.marketplace.listing.infrastructure.rest.dto.ListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/certification")
@Tag(name = "Certification", description = "Certification des annonces (CONTROLLER)")
public class CertificationController {
    private final CertifyListingUseCase certifyListingUseCase;
    private final ListPendingListingsUseCase listPendingListingsUseCase;

    public CertificationController(CertifyListingUseCase certifyListingUseCase,
                                   ListPendingListingsUseCase listPendingListingsUseCase) {
        this.certifyListingUseCase = certifyListingUseCase;
        this.listPendingListingsUseCase = listPendingListingsUseCase;
    }

    @GetMapping("/pending")
    @Operation(summary = "Lister les annonces en attente", description = "Retourne toutes les annonces au statut PENDING_CERTIFICATION. Accessible au CONTROLLER.")
    public List<ListingResponse> listPending() {
        return listPendingListingsUseCase.execute().stream().map(ListingResponse::from).toList();
    }

    @PostMapping("/{listingId}/certify")
    @Operation(summary = "Certifier une annonce", description = "Passe une annonce de PENDING_CERTIFICATION a CERTIFIED.")
    public ListingResponse certify(@PathVariable String listingId) {
        return ListingResponse.from(certifyListingUseCase.execute(listingId));
    }
}

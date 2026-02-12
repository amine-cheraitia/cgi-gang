package com.marketplace.listing.infrastructure.rest;

import com.marketplace.listing.application.usecase.CreateListingUseCase;
import com.marketplace.listing.application.usecase.ListPublicListingsUseCase;
import com.marketplace.listing.infrastructure.rest.dto.CreateListingRequest;
import com.marketplace.listing.infrastructure.rest.dto.ListingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final CreateListingUseCase createListingUseCase;
    private final ListPublicListingsUseCase listPublicListingsUseCase;

    public ListingController(CreateListingUseCase createListingUseCase,
                             ListPublicListingsUseCase listPublicListingsUseCase) {
        this.createListingUseCase = createListingUseCase;
        this.listPublicListingsUseCase = listPublicListingsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponse create(@Valid @RequestBody CreateListingRequest request) {
        return ListingResponse.from(createListingUseCase.execute(
            request.eventId(),
            request.sellerId(),
            request.price(),
            request.currency()
        ));
    }

    @GetMapping
    public List<ListingResponse> listPublic() {
        return listPublicListingsUseCase.execute().stream().map(ListingResponse::from).toList();
    }
}

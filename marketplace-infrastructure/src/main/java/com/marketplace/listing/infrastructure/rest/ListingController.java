package com.marketplace.listing.infrastructure.rest;

import com.marketplace.listing.application.usecase.CreateListingUseCase;
import com.marketplace.listing.application.usecase.ListPublicListingsUseCase;
import com.marketplace.listing.application.usecase.UploadListingAttachmentUseCase;
import com.marketplace.listing.infrastructure.rest.dto.CreateListingRequest;
import com.marketplace.listing.infrastructure.rest.dto.ListingAttachmentResponse;
import com.marketplace.listing.infrastructure.rest.dto.ListingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final CreateListingUseCase createListingUseCase;
    private final ListPublicListingsUseCase listPublicListingsUseCase;
    private final UploadListingAttachmentUseCase uploadListingAttachmentUseCase;

    public ListingController(CreateListingUseCase createListingUseCase,
                             ListPublicListingsUseCase listPublicListingsUseCase,
                             UploadListingAttachmentUseCase uploadListingAttachmentUseCase) {
        this.createListingUseCase = createListingUseCase;
        this.listPublicListingsUseCase = listPublicListingsUseCase;
        this.uploadListingAttachmentUseCase = uploadListingAttachmentUseCase;
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

    @PostMapping(value = "/{listingId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ListingAttachmentResponse uploadAttachment(@PathVariable String listingId,
                                                      @RequestParam String sellerId,
                                                      @RequestPart("file") MultipartFile file) throws java.io.IOException {
        return ListingAttachmentResponse.from(uploadListingAttachmentUseCase.execute(
            listingId,
            sellerId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes()
        ));
    }
}

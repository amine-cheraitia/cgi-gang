package com.marketplace.listing.infrastructure.rest;

import com.marketplace.listing.application.usecase.CreateListingUseCase;
import com.marketplace.listing.application.usecase.GenerateListingAttachmentUploadUrlUseCase;
import com.marketplace.listing.application.usecase.ListPublicListingsUseCase;
import com.marketplace.listing.application.usecase.UploadListingAttachmentUseCase;
import com.marketplace.listing.infrastructure.rest.dto.AttachmentUploadUrlResponse;
import com.marketplace.listing.infrastructure.rest.dto.CreateListingRequest;
import com.marketplace.listing.infrastructure.rest.dto.GenerateAttachmentUploadUrlRequest;
import com.marketplace.listing.infrastructure.rest.dto.ListingAttachmentResponse;
import com.marketplace.listing.infrastructure.rest.dto.ListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Listings", description = "Gestion des annonces de billets")
public class ListingController {
    private final CreateListingUseCase createListingUseCase;
    private final ListPublicListingsUseCase listPublicListingsUseCase;
    private final UploadListingAttachmentUseCase uploadListingAttachmentUseCase;
    private final GenerateListingAttachmentUploadUrlUseCase generateListingAttachmentUploadUrlUseCase;

    public ListingController(CreateListingUseCase createListingUseCase,
                             ListPublicListingsUseCase listPublicListingsUseCase,
                             UploadListingAttachmentUseCase uploadListingAttachmentUseCase,
                             GenerateListingAttachmentUploadUrlUseCase generateListingAttachmentUploadUrlUseCase) {
        this.createListingUseCase = createListingUseCase;
        this.listPublicListingsUseCase = listPublicListingsUseCase;
        this.uploadListingAttachmentUseCase = uploadListingAttachmentUseCase;
        this.generateListingAttachmentUploadUrlUseCase = generateListingAttachmentUploadUrlUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Creer une annonce", description = "Cree une annonce en statut PENDING_CERTIFICATION.")
    public ListingResponse create(@Valid @RequestBody CreateListingRequest request) {
        return ListingResponse.from(createListingUseCase.execute(
            request.eventId(),
            request.sellerId(),
            request.price(),
            request.currency()
        ));
    }

    @GetMapping
    @Operation(summary = "Billets certifies disponibles", description = "Retourne toutes les annonces certifiees disponibles a l'achat. Accessible sans authentification.")
    public List<ListingResponse> listPublic() {
        return listPublicListingsUseCase.execute().stream().map(ListingResponse::from).toList();
    }

    @PostMapping(value = "/{listingId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Uploader une piece justificative", description = "Upload direct via API (local ou S3 selon provider).")
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

    @PostMapping("/{listingId}/attachments/presign")
    @Operation(summary = "Generer une URL d'upload presignee", description = "Retourne une URL PUT presignee quand le provider le supporte (S3).")
    public AttachmentUploadUrlResponse presignAttachmentUpload(@PathVariable String listingId,
                                                               @Valid @RequestBody GenerateAttachmentUploadUrlRequest request) {
        return AttachmentUploadUrlResponse.from(generateListingAttachmentUploadUrlUseCase.execute(
            listingId,
            request.sellerId(),
            request.filename(),
            request.contentType()
        ));
    }
}

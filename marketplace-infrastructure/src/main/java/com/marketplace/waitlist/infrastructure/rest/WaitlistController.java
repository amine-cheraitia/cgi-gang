package com.marketplace.waitlist.infrastructure.rest;

import com.marketplace.waitlist.application.usecase.SubscribeWaitlistUseCase;
import com.marketplace.waitlist.application.usecase.UnsubscribeWaitlistUseCase;
import com.marketplace.waitlist.infrastructure.rest.dto.WaitlistSubscriptionRequest;
import com.marketplace.waitlist.infrastructure.rest.dto.WaitlistSubscriptionResponse;
import com.marketplace.user.infrastructure.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waitlist/subscriptions")
@Tag(name = "Waitlist", description = "Inscriptions en attente sur un evenement")
public class WaitlistController {
    private final SubscribeWaitlistUseCase subscribeWaitlistUseCase;
    private final UnsubscribeWaitlistUseCase unsubscribeWaitlistUseCase;
    private final CurrentUserService currentUserService;

    public WaitlistController(SubscribeWaitlistUseCase subscribeWaitlistUseCase,
                              UnsubscribeWaitlistUseCase unsubscribeWaitlistUseCase,
                              CurrentUserService currentUserService) {
        this.subscribeWaitlistUseCase = subscribeWaitlistUseCase;
        this.unsubscribeWaitlistUseCase = unsubscribeWaitlistUseCase;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "S'inscrire en waitlist", description = "Ajoute un acheteur a la waitlist d'un evenement.")
    public WaitlistSubscriptionResponse subscribe(@Valid @RequestBody WaitlistSubscriptionRequest request) {
        String userId = currentUserService.getCurrentUserId();
        return WaitlistSubscriptionResponse.from(
            subscribeWaitlistUseCase.execute(request.eventId(), userId)
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Se desinscrire de la waitlist", description = "Retire un acheteur de la waitlist d'un evenement.")
    public void unsubscribe(@RequestParam String eventId) {
        String userId = currentUserService.getCurrentUserId();
        unsubscribeWaitlistUseCase.execute(eventId, userId);
    }
}

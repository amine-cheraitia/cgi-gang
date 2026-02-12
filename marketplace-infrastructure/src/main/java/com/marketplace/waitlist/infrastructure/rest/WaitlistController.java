package com.marketplace.waitlist.infrastructure.rest;

import com.marketplace.waitlist.application.usecase.SubscribeWaitlistUseCase;
import com.marketplace.waitlist.infrastructure.rest.dto.WaitlistSubscriptionRequest;
import com.marketplace.waitlist.infrastructure.rest.dto.WaitlistSubscriptionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waitlist/subscriptions")
public class WaitlistController {
    private final SubscribeWaitlistUseCase subscribeWaitlistUseCase;

    public WaitlistController(SubscribeWaitlistUseCase subscribeWaitlistUseCase) {
        this.subscribeWaitlistUseCase = subscribeWaitlistUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistSubscriptionResponse subscribe(@Valid @RequestBody WaitlistSubscriptionRequest request) {
        return WaitlistSubscriptionResponse.from(
            subscribeWaitlistUseCase.execute(request.eventId(), request.userId())
        );
    }
}

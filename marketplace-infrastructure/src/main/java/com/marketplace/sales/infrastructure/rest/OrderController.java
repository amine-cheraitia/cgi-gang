package com.marketplace.sales.infrastructure.rest;

import com.marketplace.sales.application.usecase.GetOrderUseCase;
import com.marketplace.sales.application.usecase.MarkOrderPaidUseCase;
import com.marketplace.sales.application.usecase.PlaceOrderUseCase;
import com.marketplace.sales.infrastructure.rest.dto.OrderResponse;
import com.marketplace.sales.infrastructure.rest.dto.PlaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final MarkOrderPaidUseCase markOrderPaidUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           MarkOrderPaidUseCase markOrderPaidUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.markOrderPaidUseCase = markOrderPaidUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return OrderResponse.from(placeOrderUseCase.execute(request.listingId(), request.buyerId()));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return OrderResponse.from(getOrderUseCase.execute(orderId));
    }

    @PostMapping("/{orderId}/pay")
    public OrderResponse markOrderPaid(@PathVariable String orderId) {
        return OrderResponse.from(markOrderPaidUseCase.execute(orderId));
    }
}

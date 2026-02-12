package com.marketplace.sales.domain.repository;

import com.marketplace.sales.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(String orderId);
}

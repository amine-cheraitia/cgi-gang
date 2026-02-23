package com.marketplace.sales.infrastructure.persistence;

import com.marketplace.sales.domain.model.Order;
import com.marketplace.sales.domain.model.OrderStatus;
import com.marketplace.sales.domain.repository.OrderRepository;
import com.marketplace.sales.domain.valueobject.PricingBreakdown;
import com.marketplace.shared.domain.valueobject.Money;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.Optional;

@Repository
public class JpaOrderRepositoryAdapter implements OrderRepository {
    private final SpringDataOrderRepository repository;

    public JpaOrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return repository.findById(orderId).map(this::toDomain);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setListingId(order.getListingId());
        entity.setBuyerId(order.getBuyerId());
        entity.setSellerId(order.getSellerId());
        entity.setTicketPrice(order.getPricing().ticketPrice().amount());
        entity.setSellerFee(order.getPricing().sellerFee().amount());
        entity.setServiceFee(order.getPricing().serviceFee().amount());
        entity.setTransactionFee(order.getPricing().transactionFee().amount());
        entity.setBuyerTotal(order.getPricing().buyerTotal().amount());
        entity.setSellerPayout(order.getPricing().sellerPayout().amount());
        entity.setPlatformRevenue(order.getPricing().platformRevenue().amount());
        entity.setCurrency(order.getPricing().ticketPrice().currency().getCurrencyCode());
        entity.setStatus(order.getStatus().name());
        entity.setStripePaymentIntentId(order.getStripePaymentIntentId());
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        PricingBreakdown pricing = new PricingBreakdown(
            Money.of(entity.getTicketPrice(), currency),
            Money.of(entity.getSellerFee(), currency),
            Money.of(entity.getServiceFee(), currency),
            Money.of(entity.getTransactionFee(), currency),
            Money.of(entity.getBuyerTotal(), currency),
            Money.of(entity.getSellerPayout(), currency)
        );
        return Order.rehydrate(
            entity.getId(),
            entity.getListingId(),
            entity.getBuyerId(),
            entity.getSellerId(),
            pricing,
            OrderStatus.valueOf(entity.getStatus()),
            entity.getStripePaymentIntentId()
        );
    }
}

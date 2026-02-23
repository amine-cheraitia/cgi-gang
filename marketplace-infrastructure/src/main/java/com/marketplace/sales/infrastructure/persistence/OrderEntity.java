package com.marketplace.sales.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;
    @Column(name = "listing_id", nullable = false)
    private String listingId;
    @Column(name = "buyer_id", nullable = false)
    private String buyerId;
    @Column(name = "seller_id", nullable = false)
    private String sellerId;
    @Column(name = "ticket_price", nullable = false)
    private BigDecimal ticketPrice;
    @Column(name = "seller_fee", nullable = false)
    private BigDecimal sellerFee;
    @Column(name = "service_fee", nullable = false)
    private BigDecimal serviceFee;
    @Column(name = "transaction_fee", nullable = false)
    private BigDecimal transactionFee;
    @Column(name = "buyer_total", nullable = false)
    private BigDecimal buyerTotal;
    @Column(name = "seller_payout", nullable = false)
    private BigDecimal sellerPayout;
    @Column(name = "platform_revenue", nullable = false)
    private BigDecimal platformRevenue;
    @Column(name = "currency", nullable = false)
    private String currency;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    public BigDecimal getSellerFee() { return sellerFee; }
    public void setSellerFee(BigDecimal sellerFee) { this.sellerFee = sellerFee; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getTransactionFee() { return transactionFee; }
    public void setTransactionFee(BigDecimal transactionFee) { this.transactionFee = transactionFee; }
    public BigDecimal getBuyerTotal() { return buyerTotal; }
    public void setBuyerTotal(BigDecimal buyerTotal) { this.buyerTotal = buyerTotal; }
    public BigDecimal getSellerPayout() { return sellerPayout; }
    public void setSellerPayout(BigDecimal sellerPayout) { this.sellerPayout = sellerPayout; }
    public BigDecimal getPlatformRevenue() { return platformRevenue; }
    public void setPlatformRevenue(BigDecimal platformRevenue) { this.platformRevenue = platformRevenue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }
}

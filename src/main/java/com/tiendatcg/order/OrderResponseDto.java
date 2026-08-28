package com.tiendatcg.order;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDto {

    private Long orderId;
    private String customerEmail;
    private String customerName;
    private String shippingAddress;
    private String status;
    private long netAmount;
    private long taxAmount;
    private long total;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> items;

    public OrderResponseDto(Long orderId, String customerEmail, String customerName, String shippingAddress, String status, long netAmount, long taxAmount, long total, LocalDateTime createdAt, List<OrderItemResponseDto> items) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.total = total;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getStatus() {
        return status;
    }

    public long getNetAmount() {
        return netAmount;
    }

    public long getTaxAmount() {
        return taxAmount;
    }

    public long getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemResponseDto> getItems() {
        return items;
    }
}
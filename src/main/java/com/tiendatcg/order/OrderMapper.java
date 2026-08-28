package com.tiendatcg.order;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponseDto toItemResponse(OrderItem item) {

        return new OrderItemResponseDto(
                item.getProductId(),
                item.getCardName(),
                item.getImageUrl(),
                item.getLanguage(),
                item.getVariant(),
                item.getCondition(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal());
    }

    public OrderResponseDto toResponse(Order order) {

        List<OrderItemResponseDto> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponseDto(
                order.getId(),
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getShippingAddress(),
                order.getStatus().name(),
                order.getNetAmount(),
                order.getTaxAmount(),
                order.getTotal(),
                order.getCreatedAt(),
                items
        );
    }
}
package com.tiendatcg.cart;

import com.tiendatcg.pricing.TaxBreakdown;
import com.tiendatcg.pricing.TaxCalculator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    private final TaxCalculator taxCalculator;

    public CartMapper(TaxCalculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    public CartItemResponseDto toItemResponse(CartItem item) {
        long unitPrice = item.getProduct().getPrice();
        long subtotal = unitPrice * item.getQuantity();

        return new CartItemResponseDto(
                item.getProduct().getId(),
                item.getProduct().getCard().getName(),
                item.getProduct().getCard().getImageUrl(),
                item.getProduct().getLanguage().name(),
                item.getProduct().getVariant().name(),
                item.getProduct().getCondition().name(),
                unitPrice,
                item.getQuantity(),
                subtotal
        );
    }

    public CartResponseDto toResponse(Cart cart) {

        List<CartItemResponseDto> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        long total = items.stream()
                .mapToLong(CartItemResponseDto::getSubtotal)
                .sum();

        TaxBreakdown taxBreakdown =
                taxCalculator.calculateFromTaxIncludedTotal(total);

        return new CartResponseDto(
                cart.getId(),
                cart.getGuestToken(),
                items,
                taxBreakdown.netAmount(),
                taxBreakdown.taxAmount(),
                taxBreakdown.total()
        );
    }
}
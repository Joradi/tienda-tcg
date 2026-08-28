package com.tiendatcg.order;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.util.Objects;

@Entity
@Table(name = "order_items")
@Check(
        constraints =
                "quantity > 0 " +
                        "AND unit_price >= 0 " +
                        "AND subtotal >= 0 " +
                        "AND subtotal = unit_price * quantity"
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "card_name", nullable = false)
    private String cardName;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(nullable = false)
    private String language;
    @Column(nullable = false)
    private String variant;
    @Column(nullable = false)
    private String condition;
    @Column(name = "unit_price", nullable = false)
    private long unitPrice;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private long subtotal;

    public OrderItem() {
    }

    public OrderItem(Order order, Long productId, String cardName, String imageUrl, String language, String variant, String condition, long unitPrice,
            int quantity, long subtotal) {
        validateAmounts(unitPrice, quantity, subtotal);
        this.order = Objects.requireNonNull(order);
        this.productId = Objects.requireNonNull(productId);
        this.cardName = Objects.requireNonNull(cardName);
        this.imageUrl = imageUrl;
        this.language = Objects.requireNonNull(language);
        this.variant = Objects.requireNonNull(variant);
        this.condition = Objects.requireNonNull(condition);
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Long getProductId() {
        return productId;
    }

    public String getCardName() {
        return cardName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLanguage() {
        return language;
    }

    public String getVariant() {
        return variant;
    }

    public String getCondition() {
        return condition;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setOrder(Order order) {
        this.order = Objects.requireNonNull(order);
    }

    public void setProductId(Long productId) {
        this.productId = Objects.requireNonNull(productId);
    }

    public void setCardName(String cardName) {
        this.cardName = Objects.requireNonNull(cardName);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLanguage(String language) {
        this.language = Objects.requireNonNull(language);
    }

    public void setVariant(String variant) {
        this.variant = Objects.requireNonNull(variant);
    }

    public void setCondition(String condition) {
        this.condition = Objects.requireNonNull(condition);
    }

    public void setUnitPrice(long unitPrice) {
        if (unitPrice < 0)
        {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.unitPrice = unitPrice;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0)
        {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.quantity = quantity;
    }

    public void setSubtotal(long subtotal) {
        if (subtotal < 0)
        {
            throw new IllegalArgumentException("El subtotal no puede ser negativo");
        }
        this.subtotal = subtotal;
    }

    private void validateAmounts(long unitPrice, int quantity, long subtotal) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }

        if (quantity <= 0)
        {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        if (subtotal != unitPrice * quantity)
        {
            throw new IllegalArgumentException("El subtotal debe coincidir con precio unitario por cantidad");
        }
    }
}
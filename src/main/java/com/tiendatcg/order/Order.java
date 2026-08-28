package com.tiendatcg.order;

import com.tiendatcg.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Check(
        constraints =
                "net_amount >= 0 " +
                        "AND tax_amount >= 0 " +
                        "AND total >= 0 " +
                        "AND net_amount + tax_amount = total"
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    @Column(name = "net_amount", nullable = false)
    private long netAmount;
    @Column(name = "tax_amount", nullable = false)
    private long taxAmount;
    @Column(nullable = false)
    private long total;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(User user, String customerEmail, String customerName, String shippingAddress, OrderStatus status, long netAmount, long taxAmount, long total,
            LocalDateTime createdAt) {
        validateAmounts(netAmount, taxAmount, total);
        this.user = user;
        this.customerEmail = Objects.requireNonNull(customerEmail, "El email del cliente no puede ser null");
        this.customerName = Objects.requireNonNull(customerName, "El nombre del cliente no puede ser null");
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "La dirección de envío no puede ser null");
        this.status = Objects.requireNonNull(status, "El estado de la orden no puede ser null");
        this.netAmount = netAmount;
        this.taxAmount = taxAmount;
        this.total = total;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación no puede ser null");
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public OrderStatus getStatus() {
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = Objects.requireNonNull(customerEmail, "El email del cliente no puede ser null");
    }

    public void setCustomerName(String customerName) {
        this.customerName = Objects.requireNonNull(customerName, "El nombre del cliente no puede ser null");
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "La dirección de envío no puede ser null");
    }

    public void setStatus(OrderStatus status) {
        this.status = Objects.requireNonNull(status, "El estado de la orden no puede ser null");
    }

    public void setNetAmount(long netAmount) {
        if (netAmount < 0)
        {
            throw new IllegalArgumentException("El monto neto no puede ser negativo");
        }
        this.netAmount = netAmount;
    }

    public void setTaxAmount(long taxAmount) {
        if (taxAmount < 0)
        {
            throw new IllegalArgumentException("El impuesto no puede ser negativo");
        }
        this.taxAmount = taxAmount;
    }

    public void setTotal(long total) {
        if (total < 0)
        {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }
        this.total = total;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación no puede ser null");
    }

    private void validateAmounts(long netAmount, long taxAmount, long total) {
        if (netAmount < 0 || taxAmount < 0 || total < 0)
        {
            throw new IllegalArgumentException("Los montos de una orden no pueden ser negativos");
        }

        if (netAmount + taxAmount != total)
        {
            throw new IllegalArgumentException("El neto más el impuesto debe ser igual al total");
        }
    }
}
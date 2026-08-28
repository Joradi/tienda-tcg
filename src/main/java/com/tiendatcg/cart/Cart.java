package com.tiendatcg.cart;

import com.tiendatcg.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Check(
        constraints =
                "(user_id IS NOT NULL AND guest_token IS NULL) " +
                        "OR " +
                        "(user_id IS NULL AND guest_token IS NOT NULL)"
)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "guest_token", unique = true)
    private UUID guestToken;

    public Cart() {
    }

    public Cart(User user) {
        this.user = Objects.requireNonNull(user, "El usuario no puede ser null");
    }

    public Cart(UUID guestToken) {
        this.guestToken = Objects.requireNonNull(guestToken, "El guest token no puede ser null");
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public UUID getGuestToken() {
        return guestToken;
    }

    public void setUser(User user)
    {
        if (user != null && guestToken != null)
        {
            throw new IllegalStateException("Un carrito invitado no puede pertenecer también a un usuario");
        }

        this.user = user;
    }
}
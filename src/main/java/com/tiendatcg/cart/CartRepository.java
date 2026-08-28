package com.tiendatcg.cart;

import com.tiendatcg.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByGuestToken(UUID guestToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user = :user")
    Optional<Cart> findByUserForUpdate(@Param("user") User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.guestToken = :guestToken")
    Optional<Cart> findByGuestTokenForUpdate(
            @Param("guestToken") UUID guestToken
    );
}
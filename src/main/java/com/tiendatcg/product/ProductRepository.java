package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCardAndLanguageAndVariantAndCondition(Card card, Language language, Variant variant, Condition condition);
    List<Product> findByStockGreaterThan(int stock);
    List<Product> findByCard_Id(Long cardId);
    List<Product> findByCard_IdAndStockGreaterThan(Long cardId, int stock);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}

package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCardAndLanguageAndVariantAndCondition(Card card, Language language, Variant variant, Condition condition);
    List<Product> findByStockGreaterThan(int stock);
    List<Product> findByCard_Id(Long cardId);
    List<Product> findByCard_IdAndStockGreaterThan(Long cardId, int stock);
}

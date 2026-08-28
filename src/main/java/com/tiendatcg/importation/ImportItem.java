package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.util.Objects;

@Entity
@Table(name = "import_item")
@Check(
        constraints =
                "quantity > 0 " +
                        "AND purchase_unit_price_clp >= 0 " +
                        "AND local_reference_price_clp >= 0"
)
public class ImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "importation_id", nullable = false)
    private Importation importation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Variant variant;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "purchase_unit_price_clp", nullable = false)
    private long purchaseUnitPriceClp;
    @Column(name = "local_reference_price_clp", nullable = false)
    private long localReferencePriceClp;

    protected ImportItem() {
    }

    public ImportItem(Card card, Language language, Variant variant, Condition condition, int quantity, long purchaseUnitPriceClp, long localReferencePriceClp) {
        setCard(card);
        setLanguage(language);
        setVariant(variant);
        setCondition(condition);
        setQuantity(quantity);
        setPurchaseUnitPriceClp(purchaseUnitPriceClp);
        setLocalReferencePriceClp(localReferencePriceClp);
    }

    public Long getId() {
        return id;
    }

    public Importation getImportation() {
        return importation;
    }

    public Card getCard() {
        return card;
    }

    public Language getLanguage() {
        return language;
    }

    public Variant getVariant() {
        return variant;
    }

    public Condition getCondition() {
        return condition;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPurchaseUnitPriceClp() {
        return purchaseUnitPriceClp;
    }

    public long getLocalReferencePriceClp() {
        return localReferencePriceClp;
    }

    void setImportation(Importation importation) {
        this.importation = importation;
    }

    public void setCard(Card card)
    {
        this.card = Objects.requireNonNull(card, "La carta es obligatoria");
    }

    public void setLanguage(Language language)
    {
        this.language = Objects.requireNonNull(language, "El idioma es obligatorio");
    }

    public void setVariant(Variant variant)
    {
        this.variant = Objects.requireNonNull(variant, "La variante es obligatoria");
    }

    public void setCondition(Condition condition)
    {
        this.condition = Objects.requireNonNull(condition, "La condición es obligatoria");
    }

    public void setQuantity(int quantity)
    {
        if (quantity <= 0)
        {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        this.quantity = quantity;
    }

    public void setPurchaseUnitPriceClp(long purchaseUnitPriceClp)
    {
        validateNonNegative(purchaseUnitPriceClp, "El precio unitario de compra no puede ser negativo");

        this.purchaseUnitPriceClp = purchaseUnitPriceClp;
    }

    public void setLocalReferencePriceClp(long localReferencePriceClp)
    {
        validateNonNegative(localReferencePriceClp, "El precio de referencia local no puede ser negativo");

        this.localReferencePriceClp = localReferencePriceClp;
    }

    private void validateNonNegative(long value, String message)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
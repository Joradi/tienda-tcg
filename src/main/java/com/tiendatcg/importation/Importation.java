package com.tiendatcg.importation;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "importation")
@Check(
        constraints =
                "proxy_cost_clp >= 0 " +
                        "AND freight_cost_clp >= 0 " +
                        "AND insurance_cost_clp >= 0 " +
                        "AND other_shared_cost_clp >= 0 " +
                        "AND customs_value_usd >= 0"
)
public class Importation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportOrigin origin;
    @Column(name = "proxy_cost_clp", nullable = false)
    private long proxyCostClp;
    @Column(name = "freight_cost_clp", nullable = false)
    private long freightCostClp;
    @Column(name = "insurance_cost_clp", nullable = false)
    private long insuranceCostClp;
    @Column(name = "other_shared_cost_clp", nullable = false)
    private long otherSharedCostClp;
    @Column(
            name = "customs_value_usd",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal customsValueUsd;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(
            mappedBy = "importation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ImportItem> items = new ArrayList<>();

    protected Importation() {
    }

    public Importation(ImportOrigin origin, long proxyCostClp, long freightCostClp, long insuranceCostClp, long otherSharedCostClp, BigDecimal customsValueUsd) {
        setOrigin(origin);
        setProxyCostClp(proxyCostClp);
        setFreightCostClp(freightCostClp);
        setInsuranceCostClp(insuranceCostClp);
        setOtherSharedCostClp(otherSharedCostClp);
        setCustomsValueUsd(customsValueUsd);
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(ImportItem item)
    {
        Objects.requireNonNull(item, "El item de importación es obligatorio");
        item.setImportation(this);
        items.add(item);
    }

    public void removeItem(ImportItem item)
    {
        Objects.requireNonNull(item, "El item de importación es obligatorio");

        if (items.remove(item))
        {
            item.setImportation(null);
        }
    }

    @Transient
    public long getTotalCardQuantity()
    {
        return items.stream().mapToLong(ImportItem::getQuantity).sum();
    }

    public Long getId() {
        return id;
    }

    public ImportOrigin getOrigin() {
        return origin;
    }

    public long getProxyCostClp() {
        return proxyCostClp;
    }

    public long getFreightCostClp() {
        return freightCostClp;
    }

    public long getInsuranceCostClp() {
        return insuranceCostClp;
    }

    public long getOtherSharedCostClp() {
        return otherSharedCostClp;
    }

    public BigDecimal getCustomsValueUsd() {
        return customsValueUsd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ImportItem> getItems() {
        return items;
    }

    public void setOrigin(ImportOrigin origin)
    {
        this.origin = Objects.requireNonNull(origin, "El origen de la importación es obligatorio");
    }

    public void setProxyCostClp(long proxyCostClp)
    {
        validateNonNegative(proxyCostClp, "El costo del proxy no puede ser negativo");
        this.proxyCostClp = proxyCostClp;
    }

    public void setFreightCostClp(long freightCostClp)
    {
        validateNonNegative(freightCostClp, "El costo del flete no puede ser negativo");
        this.freightCostClp = freightCostClp;
    }

    public void setInsuranceCostClp(long insuranceCostClp)
    {
        validateNonNegative(insuranceCostClp, "El costo del seguro no puede ser negativo");

        this.insuranceCostClp = insuranceCostClp;
    }

    public void setOtherSharedCostClp(long otherSharedCostClp)
    {
        validateNonNegative(otherSharedCostClp, "Los otros costos compartidos no pueden ser negativos");
        this.otherSharedCostClp = otherSharedCostClp;
    }

    public void setCustomsValueUsd(BigDecimal customsValueUsd)
    {
        Objects.requireNonNull(customsValueUsd, "El valor aduanero en USD es obligatorio");

        if (customsValueUsd.signum() < 0)
        {
            throw new IllegalArgumentException("El valor aduanero en USD no puede ser negativo");
        }
        this.customsValueUsd = customsValueUsd;
    }

    private void validateNonNegative(long value, String message)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
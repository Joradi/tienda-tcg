package com.tiendatcg.importation;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    public static record ImportItemCostAnalysis(Long importItemId, Long cardId, String cardName, int quantity, long purchaseUnitPriceClp, long purchaseTotalClp,
            long sharedCostAllocatedClp, long sharedCostPerUnitClp, long allocatedTaxClp, long taxPerUnitClp, long landedCostTotalClp, long landedCostUnitClp,
            long localReferencePriceClp) {
    }

    @Component
    public static class ImportItemCostCalculator
    {
        private static final int CALCULATION_SCALE = 24;
        private final ImportCostCalculator costCalculator;
        private final ImportTaxCalculator taxCalculator;

        public ImportItemCostCalculator(ImportCostCalculator costCalculator, ImportTaxCalculator taxCalculator)
        {
            this.costCalculator = Objects.requireNonNull(costCalculator, "El calculador de costos es obligatorio");

            this.taxCalculator = Objects.requireNonNull(taxCalculator, "El calculador de impuestos es obligatorio");
        }

        public List<ImportItemCostAnalysis> calculateItemCosts(
                Importation importation
        ) {

            Objects.requireNonNull(importation, "La importación es obligatoria");

            ImportCostSummary costSummary = costCalculator.calculateBaseCosts(importation);
            ImportTaxSummary taxSummary = taxCalculator.calculateTaxes(importation);
            List<Long> taxAllocations = allocateTaxes(importation, costSummary.totalCardQuantity(), taxSummary);
            List<ImportItemCostAnalysis> analyses = new ArrayList<>();
            long sharedBasePerUnit = costSummary.totalSharedCostClp() / costSummary.totalCardQuantity();
            long sharedRemainder = costSummary.totalSharedCostClp() % costSummary.totalCardQuantity();

            for (int i = 0; i < importation.getItems().size(); i++)
            {
                ImportItem item = importation.getItems().get(i);
                long purchaseTotalClp = Math.multiplyExact(item.getPurchaseUnitPriceClp(),
                                item.getQuantity());

                long extraSharedUnits = Math.min((long) item.getQuantity(), sharedRemainder);
                long sharedCostAllocatedClp = Math.addExact(Math.multiplyExact(sharedBasePerUnit,
                                        item.getQuantity()), extraSharedUnits);

                sharedRemainder -= extraSharedUnits;
                long allocatedTaxClp = taxAllocations.get(i);
                long landedCostTotalClp = Math.addExact(purchaseTotalClp,
                                Math.addExact(sharedCostAllocatedClp, allocatedTaxClp));

                long sharedCostPerUnitClp = calculateAveragePerUnit(sharedCostAllocatedClp, item.getQuantity());
                long taxPerUnitClp = calculateAveragePerUnit(allocatedTaxClp, item.getQuantity());

                long landedCostUnitClp = calculateAveragePerUnit(landedCostTotalClp, item.getQuantity());

                analyses.add(new ImportItemCostAnalysis(item.getId(),
                                item.getCard().getId(),
                                item.getCard().getName(),
                                item.getQuantity(),
                                item.getPurchaseUnitPriceClp(),
                                purchaseTotalClp,
                                sharedCostAllocatedClp,
                                sharedCostPerUnitClp,
                                allocatedTaxClp,
                                taxPerUnitClp,
                                landedCostTotalClp,
                                landedCostUnitClp,
                                item.getLocalReferencePriceClp()
                        ));
            }

            return analyses;
        }

        private List<Long> allocateTaxes(Importation importation, long totalCardQuantity, ImportTaxSummary taxSummary)
        {
            int itemCount = importation.getItems().size();
            if (taxSummary.totalTaxClp() == 0L || taxSummary.customsBaseClp() == 0L)
            {
                return java.util.Collections.nCopies(itemCount, 0L);
            }

            BigDecimal sharedCustomsCostPerUnit = BigDecimal.valueOf(Math.addExact(
                                            importation.getFreightCostClp(),
                                            importation.getInsuranceCostClp()))
                            .divide(BigDecimal.valueOf(totalCardQuantity),
                                    CALCULATION_SCALE,
                                    RoundingMode.HALF_UP);

            List<TaxAllocationPart> parts = new ArrayList<>();

            long allocatedFloorTotal = 0L;

            for (int i = 0; i < itemCount; i++)
            {
                ImportItem item = importation.getItems().get(i);
                long purchaseTotalClp = Math.multiplyExact(
                                item.getPurchaseUnitPriceClp(),
                                item.getQuantity());

                BigDecimal itemCustomsBase = BigDecimal.valueOf(purchaseTotalClp)
                                .add(sharedCustomsCostPerUnit.multiply(
                                                        BigDecimal.valueOf(item.getQuantity())));

                BigDecimal exactTaxAllocation = BigDecimal.valueOf(taxSummary.totalTaxClp())
                                .multiply(itemCustomsBase)
                                .divide(BigDecimal.valueOf(taxSummary.customsBaseClp()),
                                        CALCULATION_SCALE,
                                        RoundingMode.HALF_UP);

                long floorAllocation = exactTaxAllocation.setScale(0, RoundingMode.DOWN)
                                .longValueExact();

                BigDecimal fractionalRemainder = exactTaxAllocation.subtract(BigDecimal.valueOf(floorAllocation));

                parts.add(new TaxAllocationPart(i, floorAllocation, fractionalRemainder));

                allocatedFloorTotal = Math.addExact(allocatedFloorTotal, floorAllocation);
            }

            long remainingTax = taxSummary.totalTaxClp() - allocatedFloorTotal;

            parts.sort(Comparator.comparing(TaxAllocationPart::fractionalRemainder)
                            .reversed()
            );

            long[] allocations = new long[itemCount];

            for (TaxAllocationPart part : parts)
            {
                allocations[part.index()] = part.floorAllocation();
            }

            for (long i = 0; i < remainingTax; i++)
            {

                TaxAllocationPart part = parts.get((int) (i % itemCount));

                allocations[part.index()] = Math.addExact(allocations[part.index()], 1L);
            }

            List<Long> result = new ArrayList<>(itemCount);

            for (long allocation : allocations)
            {
                result.add(allocation);
            }

            return result;
        }

        private long calculateAveragePerUnit(long totalClp, int quantity)
        {
            return BigDecimal
                    .valueOf(totalClp)
                    .divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP)
                    .longValueExact();
        }

        private record TaxAllocationPart(int index, long floorAllocation, BigDecimal fractionalRemainder)
        {
        }
    }
}
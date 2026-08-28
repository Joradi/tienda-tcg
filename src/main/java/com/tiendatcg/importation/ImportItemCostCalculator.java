package com.tiendatcg.importation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class ImportItemCostCalculator {

    private static final int CALCULATION_SCALE = 24;
    private final ImportCostCalculator costCalculator;
    private final ImportTaxCalculator taxCalculator;

    public ImportItemCostCalculator(ImportCostCalculator costCalculator, ImportTaxCalculator taxCalculator) {
        this.costCalculator = Objects.requireNonNull(costCalculator, "El calculador de costos es obligatorio");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "El calculador de impuestos es obligatorio");
    }

    public List<ImportItemCostAnalysis> calculateItemCosts(Importation importation)
    {
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

            long purchaseTotalClp = Math.multiplyExact(item.getPurchaseUnitPriceClp(), item.getQuantity());
            long extraSharedUnits = Math.min((long) item.getQuantity(), sharedRemainder);
            long sharedCostAllocatedClp = Math.addExact(Math.multiplyExact(sharedBasePerUnit, item.getQuantity()),
                            extraSharedUnits);

            sharedRemainder -= extraSharedUnits;

            long allocatedTaxClp = taxAllocations.get(i);
            long landedCostTotalClp = Math.addExact(purchaseTotalClp, Math.addExact(sharedCostAllocatedClp,
                                    allocatedTaxClp));

            long sharedCostPerUnitClp = calculateAveragePerUnit(sharedCostAllocatedClp, item.getQuantity());
            long taxPerUnitClp = calculateAveragePerUnit(allocatedTaxClp, item.getQuantity());
            long landedCostUnitClp = calculateAveragePerUnit(landedCostTotalClp, item.getQuantity());

            analyses.add(new ImportItemCostAnalysis(
                            item.getId(),
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
                            item.getLocalReferencePriceClp()));
        }

        return analyses;
    }

    private List<Long> allocateTaxes(Importation importation, long totalCardQuantity, ImportTaxSummary taxSummary)
    {
        int itemCount = importation.getItems().size();

        if (taxSummary.totalTaxClp() == 0L || taxSummary.customsBaseClp() == 0L)
        {
            return new ArrayList<>(java.util.Collections.nCopies(itemCount, 0L));
        }

        BigDecimal freightAndInsurancePerUnit = BigDecimal.valueOf(Math.addExact(
                                        importation.getFreightCostClp(),
                                        importation.getInsuranceCostClp()))
                        .divide(BigDecimal.valueOf(totalCardQuantity),
                                CALCULATION_SCALE, RoundingMode.HALF_UP);

        List<TaxAllocationPart> parts = new ArrayList<>();
        long floorTotal = 0L;
        for (int i = 0; i < itemCount; i++)
        {
            ImportItem item = importation.getItems().get(i);
            long merchandiseTotal = Math.multiplyExact(item.getPurchaseUnitPriceClp(), item.getQuantity());
            BigDecimal itemCustomsBase = BigDecimal.valueOf(merchandiseTotal)
                            .add(freightAndInsurancePerUnit
                                            .multiply(BigDecimal.valueOf(item.getQuantity())));

            BigDecimal exactTax = BigDecimal.valueOf(taxSummary.totalTaxClp())
                            .multiply(itemCustomsBase)
                            .divide(BigDecimal.valueOf(taxSummary.customsBaseClp()),
                                    CALCULATION_SCALE,
                                    RoundingMode.HALF_UP
                            );

            long floorAllocation = exactTax.setScale(0, RoundingMode.DOWN).longValueExact();
            BigDecimal fractionalRemainder = exactTax.subtract(BigDecimal.valueOf(floorAllocation));
            parts.add(new TaxAllocationPart(i, floorAllocation, fractionalRemainder));
            floorTotal = Math.addExact(floorTotal, floorAllocation);
        }

        long remainingTax = taxSummary.totalTaxClp() - floorTotal;

        parts.sort(Comparator.comparing(TaxAllocationPart::fractionalRemainder).reversed());

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

        List<Long> result = new ArrayList<>();
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
                .divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP).longValueExact();
    }

    private record TaxAllocationPart(int index, long floorAllocation, BigDecimal fractionalRemainder)
    {    }
}
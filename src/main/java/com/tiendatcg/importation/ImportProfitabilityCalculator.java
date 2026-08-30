package com.tiendatcg.importation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component
public class ImportProfitabilityCalculator {

    private static final int PERCENTAGE_SCALE = 4;
    private final ImportItemCostCalculator itemCostCalculator;
    private final BigDecimal quickSaleFactor;
    private final BigDecimal normalSaleFactor;
    private final BigDecimal slowSaleFactor;
    private final BigDecimal highMarginThreshold;
    private final BigDecimal mediumMarginThreshold;

    public ImportProfitabilityCalculator(ImportItemCostCalculator itemCostCalculator, @Value("${importation.quick-sale-factor}") BigDecimal quickSaleFactor,
                                         @Value("${importation.normal-sale-factor}") BigDecimal normalSaleFactor, @Value("${importation.slow-sale-factor}") BigDecimal slowSaleFactor,
                                         @Value("${importation.high-margin-threshold}") BigDecimal highMarginThreshold, @Value("${importation.medium-margin-threshold}") BigDecimal mediumMarginThreshold)
    {
        this.itemCostCalculator = Objects.requireNonNull(itemCostCalculator, "El calculador de costos por item es obligatorio");
        this.quickSaleFactor = validateNonNegative(quickSaleFactor, "El factor QUICK no puede ser negativo");
        this.normalSaleFactor = validateNonNegative(normalSaleFactor, "El factor NORMAL no puede ser negativo");
        this.slowSaleFactor = validateNonNegative(slowSaleFactor, "El factor SLOW no puede ser negativo");
        this.highMarginThreshold = validateNonNegative(highMarginThreshold, "El umbral HIGH no puede ser negativo");
        this.mediumMarginThreshold = validateNonNegative(mediumMarginThreshold, "El umbral MEDIUM no puede ser negativo");
    }

    public List<ImportItemProfitabilityAnalysis> analyze(Importation importation)
    {
        Objects.requireNonNull(importation, "La importación es obligatoria");

        return itemCostCalculator
                .calculateItemCosts(importation)
                .stream()
                .map(this::analyzeItem)
                .toList();
    }

    public List<SaleScenarioAnalysis> analyzePrice(long landedCostUnitClp, long localReferencePriceClp)
    {
        if(landedCostUnitClp < 0)
        {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo");
        }

        if(localReferencePriceClp <= 0)
        {
            throw new IllegalArgumentException("El precio de referencia local debe ser mayor que cero");
        }

        SaleScenarioAnalysis quick = calculateScenario(
                SaleStrategy.QUICK,
                landedCostUnitClp,
                localReferencePriceClp,
                quickSaleFactor
        );

        SaleScenarioAnalysis normal = calculateScenario(
                SaleStrategy.NORMAL,
                landedCostUnitClp,
                localReferencePriceClp,
                normalSaleFactor
        );

        SaleScenarioAnalysis slow = calculateScenario(
                SaleStrategy.SLOW,
                landedCostUnitClp,
                localReferencePriceClp,
                slowSaleFactor
        );

        return List.of(
                quick,
                normal,
                slow
        );
    }

    private ImportItemProfitabilityAnalysis analyzeItem(ImportItemCostAnalysis cost)
    {
        if (cost.localReferencePriceClp() <= 0)
        {
            throw new IllegalArgumentException("El precio de referencia local debe ser mayor que cero");
        }

        SaleScenarioAnalysis quick = calculateScenario(
                SaleStrategy.QUICK,
                cost.landedCostUnitClp(),
                cost.localReferencePriceClp(),
                quickSaleFactor
        );

        SaleScenarioAnalysis normal = calculateScenario(
                SaleStrategy.NORMAL,
                cost.landedCostUnitClp(),
                cost.localReferencePriceClp(),
                normalSaleFactor
        );

        SaleScenarioAnalysis slow = calculateScenario(
                SaleStrategy.SLOW,
                cost.landedCostUnitClp(),
                cost.localReferencePriceClp(),
                slowSaleFactor
        );

        ImportViability viability = determineViability(quick, normal);

        return new ImportItemProfitabilityAnalysis(cost, quick, normal, slow, viability);
    }

    private SaleScenarioAnalysis calculateScenario(SaleStrategy strategy, long landedCostUnitClp,
                                                   long localReferencePriceClp, BigDecimal factor)
    {
        long salePriceClp = BigDecimal.valueOf(localReferencePriceClp)
                .multiply(factor).setScale(0, RoundingMode.HALF_UP).longValueExact();

        long profitPerUnitClp = Math.subtractExact(salePriceClp, landedCostUnitClp);
        BigDecimal markup = calculateMarkup(profitPerUnitClp, landedCostUnitClp);

        BigDecimal margin = calculateMargin(profitPerUnitClp, salePriceClp);

        return new SaleScenarioAnalysis(strategy, salePriceClp, profitPerUnitClp, markup, margin);
    }

    private ImportViability determineViability(SaleScenarioAnalysis quick, SaleScenarioAnalysis normal)
    {
        if (normal.profitPerUnitClp() <= 0)
        {
            return ImportViability.NOT_VIABLE;
        }

        if (normal.margin().compareTo(highMarginThreshold) >= 0 && quick.profitPerUnitClp() > 0)
        {
            return ImportViability.HIGH;
        }

        if (normal.margin().compareTo(mediumMarginThreshold) >= 0 && quick.profitPerUnitClp() >= 0)
        {
            return ImportViability.MEDIUM;
        }

        return ImportViability.LOW;
    }

    private BigDecimal calculateMarkup(long profitClp, long landedCostClp)
    {
        if (landedCostClp == 0L)
        {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(profitClp).divide(BigDecimal.valueOf(landedCostClp), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMargin(long profitClp, long salePriceClp)
    {
        if (salePriceClp == 0L)
        {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(profitClp).divide(BigDecimal.valueOf(salePriceClp), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal validateNonNegative(BigDecimal value, String message)
    {
        Objects.requireNonNull(value, message);
        if (value.signum() < 0)
        {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
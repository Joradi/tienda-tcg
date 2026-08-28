package com.tiendatcg.importation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Component
public class ImportTaxCalculator {
    private final ImportCostCalculator costCalculator;
    private final BigDecimal customsThresholdUsd;
    private final BigDecimal adValoremRate;
    private final BigDecimal importVatRate;

    public ImportTaxCalculator(ImportCostCalculator costCalculator, @Value("${importation.customs-threshold-usd}") BigDecimal customsThresholdUsd,
                               @Value("${importation.ad-valorem-rate}") BigDecimal adValoremRate, @Value("${importation.import-vat-rate}") BigDecimal importVatRate)
    {
        this.costCalculator = Objects.requireNonNull(costCalculator, "El calculador de costos es obligatorio");
        this.customsThresholdUsd = validateNonNegative(customsThresholdUsd, "El umbral aduanero no puede ser negativo");
        this.adValoremRate = validateNonNegative(adValoremRate, "La tasa ad valorem no puede ser negativa");
        this.importVatRate = validateNonNegative(importVatRate, "La tasa de IVA no puede ser negativa");
    }

    public ImportTaxSummary calculateTaxes(Importation importation)
    {

        Objects.requireNonNull(importation, "La importación es obligatoria");
        ImportCostSummary baseCosts = costCalculator.calculateBaseCosts(importation);
        long customsBaseClp = Math.addExact(baseCosts.merchandiseCostClp(),
                        Math.addExact(importation.getFreightCostClp(),
                                importation.getInsuranceCostClp()));

        boolean adValoremApplied = importation.getCustomsValueUsd().compareTo(customsThresholdUsd) > 0;
        long adValoremClp = 0L;

        if (adValoremApplied)
        {
            adValoremClp = calculatePercentage(customsBaseClp, adValoremRate);
        }

        long vatBaseClp = Math.addExact(customsBaseClp, adValoremClp);
        long importVatClp = calculatePercentage(vatBaseClp, importVatRate);
        long totalTaxClp = Math.addExact(adValoremClp, importVatClp);
        long landedCostTotalClp = Math.addExact(baseCosts.baseCostClp(), totalTaxClp);
        long taxPerCardClp = totalTaxClp / baseCosts.totalCardQuantity();
        long averageLandedCostPerCardClp = landedCostTotalClp / baseCosts.totalCardQuantity();
        return new ImportTaxSummary(customsBaseClp,
                adValoremApplied,
                adValoremClp,
                vatBaseClp,
                importVatClp,
                totalTaxClp,
                taxPerCardClp,
                landedCostTotalClp,
                averageLandedCostPerCardClp
        );
    }

    private long calculatePercentage(long amountClp, BigDecimal rate)
    {
        return BigDecimal
                .valueOf(amountClp)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
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
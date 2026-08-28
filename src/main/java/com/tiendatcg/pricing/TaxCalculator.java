package com.tiendatcg.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TaxCalculator {
    private final BigDecimal taxRate;
    public TaxCalculator(@Value("${store.tax-rate}") BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public TaxBreakdown calculateFromTaxIncludedTotal(long total)
    {
        if(total < 0){
            throw new IllegalArgumentException("El total no puede ser negativo");
        }

        BigDecimal totalAmount = BigDecimal.valueOf(total);
        BigDecimal divisor = BigDecimal.ONE.add(taxRate);
        long netAmount = totalAmount.divide(divisor, 0, RoundingMode.HALF_UP).longValue();
        long taxAmount = total - netAmount;
        return new TaxBreakdown(netAmount, taxAmount, total);
    }
}
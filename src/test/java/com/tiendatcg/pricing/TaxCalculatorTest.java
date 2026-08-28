package com.tiendatcg.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaxCalculatorTest {

    @Test
    void shouldCalculateTaxBreakdownFromTaxIncludedTotal() {

        TaxCalculator calculator = new TaxCalculator(new BigDecimal("0.19"));
        TaxBreakdown result = calculator.calculateFromTaxIncludedTotal(15000);
        assertEquals(12605, result.netAmount());
        assertEquals(2395, result.taxAmount());
        assertEquals(15000, result.total());
    }
}
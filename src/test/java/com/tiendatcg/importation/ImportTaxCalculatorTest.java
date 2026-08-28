package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImportTaxCalculatorTest {

    private ImportTaxCalculator taxCalculator;

    @BeforeEach
    void setUp()
    {
        ImportCostCalculator costCalculator = new ImportCostCalculator();
        taxCalculator = new ImportTaxCalculator(
                        costCalculator,
                        new BigDecimal("500"),
                        new BigDecimal("0.06"),
                        new BigDecimal("0.19")
                );
    }

    @Test
    void shouldApplyOnlyVatWhenImportationIsBelowThreshold()
    {
        Importation importation = createImportation(new BigDecimal("400.00"));
        ImportTaxSummary result = taxCalculator.calculateTaxes(importation);
        assertEquals(100000L, result.customsBaseClp());
        assertFalse(result.adValoremApplied());
        assertEquals(0L, result.adValoremClp());
        assertEquals(100000L, result.vatBaseClp());
        assertEquals(19000L, result.importVatClp());
        assertEquals(19000L, result.totalTaxClp());
        assertEquals(129000L, result.landedCostTotalClp());
        assertEquals(9500L, result.taxPerCardClp());
        assertEquals(64500L, result.averageLandedCostPerCardClp());
    }

    @Test
    void shouldNotApplyAdValoremAtExactlyFiveHundredUsd()
    {
        Importation importation = createImportation(
                new BigDecimal("500.00"));

        ImportTaxSummary result = taxCalculator.calculateTaxes(
                importation);

        assertFalse(result.adValoremApplied());
        assertEquals(0L, result.adValoremClp());
        assertEquals(19000L, result.importVatClp());
    }

    @Test
    void shouldApplyAdValoremAndVatAboveThreshold()
    {
        Importation importation = createImportation(new BigDecimal("501.00"));
        ImportTaxSummary result = taxCalculator.calculateTaxes(importation);
        assertEquals(100000L, result.customsBaseClp());
        assertTrue(result.adValoremApplied());
        assertEquals(6000L, result.adValoremClp());
        assertEquals(106000L, result.vatBaseClp());
        assertEquals(20140L, result.importVatClp());
        assertEquals(26140L, result.totalTaxClp());
        assertEquals(136140L, result.landedCostTotalClp());
        assertEquals(13070L, result.taxPerCardClp());
        assertEquals(68070L, result.averageLandedCostPerCardClp());
    }

    @Test
    void shouldExcludeProxyAndOtherCostsFromCustomsBaseButIncludeThemInLandedCost()
    {
        Importation importation = createImportation(new BigDecimal("501.00"));
        ImportTaxSummary result = taxCalculator.calculateTaxes(importation);
        assertEquals(100000L, result.customsBaseClp());
        assertEquals(136140L, result.landedCostTotalClp());
    }

    private Importation createImportation(BigDecimal customsValueUsd)
    {
        Importation importation = new Importation(
                        ImportOrigin.USA,
                        10000L,
                        20000L,
                        0L,
                        0L,
                        customsValueUsd
                );
        Card card = new Card();
        card.setName("Test Card");
        ImportItem item = new ImportItem(
                        card,
                        Language.ENGLISH,
                        Variant.NORMAL,
                        Condition.NEAR_MINT,
                        2,
                        40000L,
                        70000L
                );
        importation.addItem(item);
        return importation;
    }
}
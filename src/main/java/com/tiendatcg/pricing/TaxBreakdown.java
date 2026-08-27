package com.tiendatcg.pricing;

public record TaxBreakdown(
    long netAmount,
    long taxAmount,
    long total
    )
{
}

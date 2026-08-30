export type SaleStrategy =
    'QUICK' |
    'NORMAL' |
    'SLOW';

export type ImportViability =
    'HIGH' |
    'MEDIUM' |
    'LOW' |
    'NOT_VIABLE';

export interface ImportItemCreateRequest {
    cardId: number;
    language: string;
    variant: string;
    condition: string;
    quantity: number;
    purchaseUnitPriceClp: number;
    localReferencePriceClp: number;
}

export interface ImportationCreateRequest {
    origin: string;
    proxyCostClp: number;
    freightCostClp: number;
    insuranceCostClp: number;
    otherSharedCostClp: number;
    customsValueUsd: number;
    items: ImportItemCreateRequest[];
}

export interface ImportItemResponse {
    id: number;
    cardId: number;
    cardName: string;
    language: string;
    variant: string;
    condition: string;
    quantity: number;
    purchaseUnitPriceClp: number;
    localReferencePriceClp: number;
}

export interface ImportationResponse {
    id: number;
    origin: string;
    proxyCostClp: number;
    freightCostClp: number;
    insuranceCostClp: number;
    otherSharedCostClp: number;
    customsValueUsd: number;
    totalCardQuantity: number;
    createdAt: string;
    items: ImportItemResponse[];
}

export interface ImportItemCostAnalysis {
    importItemId: number;
    cardId: number;
    cardName: string;
    quantity: number;
    purchaseUnitPriceClp: number;
    purchaseTotalClp: number;
    sharedCostAllocatedClp: number;
    sharedCostPerUnitClp: number;
    allocatedTaxClp: number;
    taxPerUnitClp: number;
    landedCostTotalClp: number;
    landedCostUnitClp: number;
    localReferencePriceClp: number;
}

export interface SaleScenarioAnalysis {
    strategy: SaleStrategy;
    salePriceClp: number;
    profitPerUnitClp: number;
    markup: number;
    margin: number;
}

export interface PriceAnalysisRequest {
    landedCostUnitClp: number;
    localReferencePriceClp: number;
}

export interface ImportItemProfitabilityAnalysis {
    cost: ImportItemCostAnalysis;
    quick: SaleScenarioAnalysis;
    normal: SaleScenarioAnalysis;
    slow: SaleScenarioAnalysis;
    viability: ImportViability;
}

export interface ImportationStrategySummary {
    strategy: SaleStrategy;
    potentialRevenueClp: number;
    potentialProfitClp: number;
    margin: number;
}

export interface ImportationAnalysis {
    totalItemCount: number;
    totalCardQuantity: number;
    merchandiseCostClp: number;
    totalSharedCostClp: number;
    totalTaxClp: number;
    landedCostTotalClp: number;
    quick: ImportationStrategySummary;
    normal: ImportationStrategySummary;
    slow: ImportationStrategySummary;
    highCardQuantity: number;
    mediumCardQuantity: number;
    lowCardQuantity: number;
    notViableCardQuantity: number;
    items: ImportItemProfitabilityAnalysis[];
}

export interface ImportScenarioComparisonRequest {
    firstScenarioImportationIds: number[];
    secondScenarioImportationIds: number[];
}

export interface ImportScenarioSummary {
    importationCount: number;
    totalCardQuantity: number;
    merchandiseCostClp: number;
    totalSharedCostClp: number;
    totalTaxClp: number;
    landedCostTotalClp: number;
    quick: ImportationStrategySummary;
    normal: ImportationStrategySummary;
    slow: ImportationStrategySummary;
    highCardQuantity: number;
    mediumCardQuantity: number;
    lowCardQuantity: number;
    notViableCardQuantity: number;
}

export interface ImportScenarioComparison {
    firstScenario: ImportScenarioSummary;
    secondScenario: ImportScenarioSummary;
    landedCostDifferenceClp: number;
    quickProfitDifferenceClp: number;
    normalProfitDifferenceClp: number;
    slowProfitDifferenceClp: number;
}
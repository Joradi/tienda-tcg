package com.tiendatcg.importation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ImportAnalysisServiceTest {

    private ImportationRepository importationRepository;
    private ImportationAnalysisCalculator analysisCalculator;
    private ImportScenarioCalculator scenarioCalculator;

    private ImportAnalysisService service;

    @BeforeEach
    void setUp()
    {
        importationRepository = mock(ImportationRepository.class);

        analysisCalculator = mock(ImportationAnalysisCalculator.class);

        scenarioCalculator = mock(ImportScenarioCalculator.class);

        service = new ImportAnalysisService(importationRepository, analysisCalculator, scenarioCalculator);
    }

    @Test
    void shouldAnalyzeExistingImportation()
    {
        Importation importation = mock(Importation.class);

        ImportationAnalysis analysis = mock(ImportationAnalysis.class);

        when(importationRepository.findWithItemsById(10L)).thenReturn(Optional.of(importation));

        when(analysisCalculator.analyze(importation)).thenReturn(analysis);

        ImportationAnalysis result = service.analyzeImportation(10L);

        assertSame(analysis, result);

        verify(analysisCalculator).analyze(importation);
    }

    @Test
    void shouldThrowWhenImportationForAnalysisDoesNotExist()
    {

        when(importationRepository.findWithItemsById(99L)).thenReturn(Optional.empty());

        assertThrows(ImportationNotFoundException.class, () -> service.analyzeImportation(99L));

        verifyNoInteractions(analysisCalculator);
    }

    @Test
    void shouldCompareTwoImportationScenarios()
    {
        Importation first = mock(Importation.class);

        Importation second = mock(Importation.class);

        Importation third = mock(Importation.class);

        ImportScenarioComparison comparison = mock(ImportScenarioComparison.class);

        when(importationRepository.findWithItemsById(1L)).thenReturn(Optional.of(first));

        when(importationRepository.findWithItemsById(2L)).thenReturn(Optional.of(second));

        when(importationRepository.findWithItemsById(3L)).thenReturn(Optional.of(third));

        when(scenarioCalculator.compare(List.of(first), List.of(second, third))).thenReturn(comparison);

        ImportScenarioComparisonRequest request = new ImportScenarioComparisonRequest(List.of(1L), List.of(2L, 3L));

        ImportScenarioComparison result = service.compareScenarios(request);

        assertSame(comparison, result);

        verify(scenarioCalculator).compare(List.of(first), List.of(second, third));
    }

    @Test
    void shouldStopScenarioComparisonWhenImportationDoesNotExist()
    {
        Importation first = mock(Importation.class);

        when(importationRepository.findWithItemsById(1L)).thenReturn(Optional.of(first));

        when(importationRepository.findWithItemsById(99L)).thenReturn(Optional.empty());

        ImportScenarioComparisonRequest request = new ImportScenarioComparisonRequest(List.of(1L), List.of(99L));

        assertThrows(ImportationNotFoundException.class, () -> service.compareScenarios(request));

        verifyNoInteractions(scenarioCalculator);
    }
}
package com.tiendatcg.importation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ImportAnalysisService {

    private final ImportationRepository importationRepository;
    private final ImportationAnalysisCalculator analysisCalculator;
    private final ImportScenarioCalculator scenarioCalculator;

    public ImportAnalysisService(ImportationRepository importationRepository, ImportationAnalysisCalculator analysisCalculator, ImportScenarioCalculator scenarioCalculator) {
        this.importationRepository = Objects.requireNonNull(importationRepository,
                        "El repositorio de importaciones es obligatorio");

        this.analysisCalculator = Objects.requireNonNull(analysisCalculator,
                        "El calculador de análisis es obligatorio");

        this.scenarioCalculator = Objects.requireNonNull(scenarioCalculator,
                        "El calculador de escenarios es obligatorio");
    }

    @Transactional(readOnly = true)
    public ImportationAnalysis analyzeImportation(Long importationId)
    {
        Importation importation = findImportation(importationId);
        return analysisCalculator.analyze(importation);
    }

    @Transactional(readOnly = true)
    public ImportScenarioComparison compareScenarios(ImportScenarioComparisonRequest request)
    {
        Objects.requireNonNull(request, "La solicitud de comparación es obligatoria");

        List<Importation> firstScenario = loadImportations(request.firstScenarioImportationIds());

        List<Importation> secondScenario = loadImportations(request.secondScenarioImportationIds());

        return scenarioCalculator.compare(firstScenario, secondScenario);
    }

    private List<Importation> loadImportations(List<Long> importationIds)
    {
        return importationIds
                .stream()
                .map(this::findImportation)
                .toList();
    }

    private Importation findImportation(Long importationId)
    {
        return importationRepository
                .findWithItemsById(importationId)
                .orElseThrow(
                        () -> new ImportationNotFoundException(importationId));
    }
}
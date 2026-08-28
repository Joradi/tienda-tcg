package com.tiendatcg.importation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/importations")
public class ImportationController {

    private final ImportationService importationService;
    private final ImportAnalysisService analysisService;

    public ImportationController(ImportationService importationService, ImportAnalysisService analysisService)
    {
        this.importationService = importationService;
        this.analysisService = analysisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportationResponseDto createImportation(@Valid @RequestBody ImportationCreateRequest request)
    {
        return importationService.createImportation(request);
    }

    @GetMapping
    public List<ImportationResponseDto> getImportations() {
        return importationService.getImportations();
    }

    @GetMapping("/{id}")
    public ImportationResponseDto getImportation(@PathVariable Long id)
    {
        return importationService.getImportation(id);
    }

    @GetMapping("/{id}/analysis")
    public ImportationAnalysis analyzeImportation(@PathVariable Long id)
    {
        return analysisService.analyzeImportation(id);
    }

    @PostMapping("/scenarios/compare")
    public ImportScenarioComparison compareScenarios(@Valid @RequestBody ImportScenarioComparisonRequest request)
    {
        return analysisService.compareScenarios(request);
    }
}
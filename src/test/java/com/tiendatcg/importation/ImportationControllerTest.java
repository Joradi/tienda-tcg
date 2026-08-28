package com.tiendatcg.importation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImportationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportationService importationService;

    @MockitoBean
    private ImportAnalysisService analysisService;

    @Test
    void shouldCreateImportation() throws Exception {

        when(importationService.createImportation(any(ImportationCreateRequest.class))).thenReturn(null);

        mockMvc.perform(post("/importations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "origin": "USA",
                                          "proxyCostClp": 1000,
                                          "freightCostClp": 5000,
                                          "insuranceCostClp": 0,
                                          "otherSharedCostClp": 0,
                                          "customsValueUsd": 200.00,
                                          "items": [
                                            {
                                              "cardId": 1,
                                              "language": "ENGLISH",
                                              "variant": "NORMAL",
                                              "condition": "NEAR_MINT",
                                              "quantity": 1,
                                              "purchaseUnitPriceClp": 10000,
                                              "localReferencePriceClp": 20000
                                            }
                                          ]
                                        }
                                        """)
                )
                .andExpect(
                        status().isCreated());

        verify(importationService
        ).createImportation(any(ImportationCreateRequest.class));
    }

    @Test
    void shouldGetImportations() throws Exception
    {
        when(importationService.getImportations()).thenReturn(List.of());

        mockMvc.perform(get("/importations")).andExpect(status().isOk());

        verify(importationService).getImportations();
    }

    @Test
    void shouldGetImportationById() throws Exception
    {
        when(importationService.getImportation(10L)).thenReturn(null);

        mockMvc.perform(get("/importations/10")).andExpect(status().isOk());

        verify(importationService).getImportation(10L);
    }

    @Test
    void shouldAnalyzeImportation() throws Exception
    {
        when(analysisService.analyzeImportation(10L)).thenReturn(null);
        mockMvc.perform(get("/importations/10/analysis"))
                .andExpect(status().isOk());

        verify(analysisService).analyzeImportation(10L);
    }

    @Test
    void shouldCompareScenarios() throws Exception
    {
        when(analysisService.compareScenarios(any(ImportScenarioComparisonRequest.class))).thenReturn(null);

        mockMvc.perform(post("/importations/scenarios/compare")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "firstScenarioImportationIds": [1],
                                          "secondScenarioImportationIds": [2, 3]
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(analysisService
        ).compareScenarios(any(ImportScenarioComparisonRequest.class));
    }

    @Test
    void shouldRejectInvalidScenarioComparisonRequest() throws Exception
    {
        mockMvc.perform(post("/importations/scenarios/compare")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "firstScenarioImportationIds": [],
                                          "secondScenarioImportationIds": []
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(analysisService, never()).compareScenarios(
                any(ImportScenarioComparisonRequest.class));
    }
}
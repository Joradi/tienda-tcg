package com.tiendatcg.importation;

import com.tiendatcg.card.Card;
import com.tiendatcg.card.CardRepository;
import com.tiendatcg.product.Condition;
import com.tiendatcg.product.Language;
import com.tiendatcg.product.Variant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportationServiceTest {

    @Mock
    private ImportationRepository importationRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ImportationMapper importationMapper;

    private ImportationService importationService;

    @BeforeEach
    void setUp()
    {
        importationService = new ImportationService(importationRepository, cardRepository, importationMapper);
    }

    @Test
    void shouldCreateImportationWithMultipleItems()
    {
        Card firstCard = mock(Card.class);
        Card secondCard = mock(Card.class);

        when(cardRepository.findById(10L))
                .thenReturn(Optional.of(firstCard));

        when(cardRepository.findById(20L))
                .thenReturn(Optional.of(secondCard));

        ImportationCreateRequest request =
                new ImportationCreateRequest(
                        ImportOrigin.USA,
                        30000L,
                        80000L,
                        5000L,
                        10000L,
                        new BigDecimal("480.00"),
                        List.of(
                                new ImportItemCreateRequest(
                                        10L,
                                        Language.ENGLISH,
                                        Variant.NORMAL,
                                        Condition.NEAR_MINT,
                                        3,
                                        15000L,
                                        28000L
                                ),
                                new ImportItemCreateRequest(
                                        20L,
                                        Language.ENGLISH,
                                        Variant.NORMAL,
                                        Condition.NEAR_MINT,
                                        2,
                                        25000L,
                                        42000L
                                )
                        )
                );

        when(importationRepository.save(any(Importation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ImportationResponseDto expectedResponse =
                new ImportationResponseDto(
                        null,
                        "USA",
                        30000L,
                        80000L,
                        5000L,
                        10000L,
                        new BigDecimal("480.00"),
                        5L,
                        LocalDateTime.now(),
                        List.of()
                );

        when(importationMapper.toResponseDto(any(Importation.class)))
                .thenReturn(expectedResponse);

        ImportationResponseDto result = importationService.createImportation(request);

        ArgumentCaptor<Importation> captor = ArgumentCaptor.forClass(Importation.class);

        verify(importationRepository).save(captor.capture());

        Importation savedImportation = captor.getValue();

        assertEquals(ImportOrigin.USA, savedImportation.getOrigin());

        assertEquals(30000L, savedImportation.getProxyCostClp());

        assertEquals(80000L, savedImportation.getFreightCostClp());

        assertEquals(2, savedImportation.getItems().size());

        assertEquals(5L, savedImportation.getTotalCardQuantity());

        assertSame(firstCard, savedImportation.getItems().get(0).getCard());

        assertSame(secondCard, savedImportation.getItems().get(1).getCard());

        assertSame(savedImportation, savedImportation.getItems().get(0).getImportation());

        assertSame(savedImportation, savedImportation.getItems().get(1).getImportation());

        assertSame(expectedResponse, result);

        verify(cardRepository).findById(10L);
        verify(cardRepository).findById(20L);

        verify(importationMapper).toResponseDto(savedImportation);
    }

    @Test
    void shouldThrowWhenCardDoesNotExist()
    {
        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        ImportationCreateRequest request =
                new ImportationCreateRequest(
                        ImportOrigin.JAPAN,
                        20000L,
                        60000L,
                        0L,
                        0L,
                        new BigDecimal("200.00"),
                        List.of(
                                new ImportItemCreateRequest(
                                        999L,
                                        Language.ENGLISH,
                                        Variant.NORMAL,
                                        Condition.NEAR_MINT,
                                        1,
                                        12000L,
                                        20000L
                                )
                        )
                );

        ImportationCardNotFoundException exception =
                assertThrows(
                        ImportationCardNotFoundException.class,
                        () -> importationService
                                .createImportation(request)
                );

        assertTrue(
                exception.getMessage()
                        .contains("999")
        );

        verify(importationRepository, never()).save(any());

        verifyNoInteractions(importationMapper);
    }

    @Test
    void shouldGetImportationById()
    {
        Importation importation = new Importation(
                        ImportOrigin.USA,
                        30000L,
                        70000L,
                        0L,
                        5000L,
                        new BigDecimal("450.00")
                );

        ImportationResponseDto expectedResponse = new ImportationResponseDto(
                        1L,
                        "USA",
                        30000L,
                        70000L,
                        0L,
                        5000L,
                        new BigDecimal("450.00"),
                        0L,
                        LocalDateTime.now(),
                        List.of()
                );

        when(importationRepository.findWithItemsById(1L))
                .thenReturn(Optional.of(importation));

        when(importationMapper.toResponseDto(importation))
                .thenReturn(expectedResponse);

        ImportationResponseDto result =
                importationService.getImportation(1L);

        assertSame(expectedResponse, result);

        verify(importationRepository).findWithItemsById(1L);

        verify(importationMapper).toResponseDto(importation);
    }

    @Test
    void shouldThrowWhenImportationDoesNotExist()
    {
        when(importationRepository.findWithItemsById(999L))
                .thenReturn(Optional.empty());

        ImportationNotFoundException exception =
                assertThrows(ImportationNotFoundException.class,
                        () -> importationService.getImportation(999L));

        assertTrue(exception.getMessage().contains("999"));

        verifyNoInteractions(importationMapper);
    }

    @Test
    void shouldGetImportationsOrderedByCreationDate()
    {
        Importation first = new Importation(
                        ImportOrigin.USA,
                        10000L,
                        20000L,
                        0L,
                        0L,
                        new BigDecimal("100.00")
                );

        Importation second = new Importation(
                        ImportOrigin.JAPAN,
                        20000L,
                        40000L,
                        0L,
                        5000L,
                        new BigDecimal("250.00")
                );

        ImportationResponseDto firstResponse = new ImportationResponseDto(
                        1L,
                        "USA",
                        10000L,
                        20000L,
                        0L,
                        0L,
                        new BigDecimal("100.00"),
                        0L,
                        LocalDateTime.now(),
                        List.of()
                );

        ImportationResponseDto secondResponse = new ImportationResponseDto(
                        2L,
                        "JAPAN",
                        20000L,
                        40000L,
                        0L,
                        5000L,
                        new BigDecimal("250.00"),
                        0L,
                        LocalDateTime.now(),
                        List.of()
                );

        when(importationRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(second, first));

        when(importationMapper.toResponseDto(second))
                .thenReturn(secondResponse);

        when(importationMapper.toResponseDto(first))
                .thenReturn(firstResponse);

        List<ImportationResponseDto> result =
                importationService.getImportations();

        assertEquals(2, result.size());

        assertSame(secondResponse, result.get(0));

        assertSame(firstResponse, result.get(1));

        verify(importationRepository).findAllByOrderByCreatedAtDesc();

        verify(importationMapper).toResponseDto(second);

        verify(importationMapper).toResponseDto(first);
    }
}
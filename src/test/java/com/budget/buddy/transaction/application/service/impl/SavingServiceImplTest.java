package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.saving.SavingDTO;
import com.budget.buddy.transaction.domain.service.SavingData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingServiceImplTest {

    @Mock
    private SavingData savingData;

    @InjectMocks
    private SavingServiceImpl savingService;

    @Test
    void create_delegatesToData() {
        SavingDTO dto = new SavingDTO(null, 1L, null, "Vacation Fund",
                new BigDecimal("2000"), "SGD", LocalDate.now(), "notes", null);

        savingService.create(dto);

        verify(savingData, times(1)).create(dto);
    }

    @Test
    void update_delegatesToData() {
        Long id = 5L;
        SavingDTO dto = new SavingDTO(null, 1L, null, "Vacation Fund",
                new BigDecimal("2500"), "SGD", LocalDate.now(), "updated notes", null);

        savingService.update(id, dto);

        verify(savingData, times(1)).update(id, dto);
    }

    @Test
    void delete_delegatesToData() {
        Long id = 5L;

        savingService.delete(id);

        verify(savingData, times(1)).delete(id);
    }

    @Test
    void deleteAll_delegatesToData() {
        List<Long> ids = List.of(1L, 2L, 3L);

        savingService.deleteAll(ids);

        verify(savingData, times(1)).deleteAll(ids);
    }

    @Test
    void getById_delegatesToData() {
        Long id = 5L;
        SavingDTO expected = new SavingDTO(id, 1L, "Account", "Vacation Fund",
                new BigDecimal("2000"), "SGD", LocalDate.now(), "notes", null);
        when(savingData.getById(id)).thenReturn(expected);

        SavingDTO result = savingService.getById(id);

        assertSame(expected, result);
        verify(savingData, times(1)).getById(id);
    }

    @Test
    void getAll_withCurrency_delegatesToData() {
        String currency = "SGD";
        SavingDTO dto = new SavingDTO(1L, 1L, "Account", "Vacation Fund",
                new BigDecimal("2000"), currency, LocalDate.now(), "notes", null);
        List<SavingDTO> expected = List.of(dto);
        when(savingData.getAll(currency)).thenReturn(expected);

        List<SavingDTO> result = savingService.getAll(currency);

        assertSame(expected, result);
        verify(savingData, times(1)).getAll(currency);
    }

    @Test
    void getAll_withoutCurrency_delegatesToData() {
        SavingDTO dto = new SavingDTO(1L, 1L, "Account", "Vacation Fund",
                new BigDecimal("2000"), "SGD", LocalDate.now(), "notes", null);
        List<SavingDTO> expected = List.of(dto);
        when(savingData.getAll(null)).thenReturn(expected);

        List<SavingDTO> result = savingService.getAll(null);

        assertSame(expected, result);
        verify(savingData, times(1)).getAll(null);
    }
}

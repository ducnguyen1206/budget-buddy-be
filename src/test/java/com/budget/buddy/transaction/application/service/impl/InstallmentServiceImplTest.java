package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;
import com.budget.buddy.transaction.domain.service.InstallmentData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstallmentServiceImplTest {

    @Mock
    private InstallmentData installmentData;

    @InjectMocks
    private InstallmentServiceImpl installmentService;

    @Test
    void create_delegatesToData() {
        InstallmentDTO dto = new InstallmentDTO(null, 1L, null, "Car Loan",
                new BigDecimal("50000"), new BigDecimal("10000"), null,
                LocalDate.now(), null, null);

        installmentService.create(dto);

        verify(installmentData, times(1)).create(dto);
    }

    @Test
    void update_delegatesToData() {
        Long id = 5L;
        InstallmentDTO dto = new InstallmentDTO(null, 1L, null, "Car Loan",
                new BigDecimal("50000"), new BigDecimal("15000"), null,
                LocalDate.now(), null, null);

        installmentService.update(id, dto);

        verify(installmentData, times(1)).update(id, dto);
    }

    @Test
    void delete_delegatesToData() {
        Long id = 5L;

        installmentService.delete(id);

        verify(installmentData, times(1)).delete(id);
    }

    @Test
    void deleteAll_delegatesToData() {
        List<Long> ids = List.of(1L, 2L, 3L);

        installmentService.deleteAll(ids);

        verify(installmentData, times(1)).deleteAll(ids);
    }

    @Test
    void getById_delegatesToData() {
        Long id = 5L;
        InstallmentDTO expected = new InstallmentDTO(id, 1L, "Account", "Car Loan",
                new BigDecimal("50000"), new BigDecimal("10000"), new BigDecimal("40000"),
                LocalDate.now(), "SGD", null);
        when(installmentData.getById(id)).thenReturn(expected);

        InstallmentDTO result = installmentService.getById(id);

        assertSame(expected, result);
        verify(installmentData, times(1)).getById(id);
    }

    @Test
    void getAll_delegatesToData() {
        InstallmentDTO dto = new InstallmentDTO(1L, 1L, "Account", "Car Loan",
                new BigDecimal("50000"), new BigDecimal("10000"), new BigDecimal("40000"),
                LocalDate.now(), "SGD", null);
        List<InstallmentDTO> expected = List.of(dto);
        when(installmentData.getAll()).thenReturn(expected);

        List<InstallmentDTO> result = installmentService.getAll();

        assertSame(expected, result);
        verify(installmentData, times(1)).getAll();
    }
}

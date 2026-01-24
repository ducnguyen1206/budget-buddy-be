package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.threshold.ThresholdDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ThresholdDataService {

    ThresholdDTO create(Long categoryId, BigDecimal threshold, String currency);

    ThresholdDTO view(Long id);

    List<ThresholdDTO> viewAll();

    ThresholdDTO update(Long id, Long categoryId, BigDecimal threshold, String currency);

    void delete(Long id);
}

package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.threshold.ThresholdDTO;
import com.budget.buddy.transaction.application.dto.threshold.ThresholdRequestDTO;

import java.util.List;

public interface ThresholdService {

    ThresholdDTO create(ThresholdRequestDTO request);

    ThresholdDTO view(Long id);

    List<ThresholdDTO> viewAll();

    ThresholdDTO update(Long id, ThresholdRequestDTO request);

    void delete(Long id);
}

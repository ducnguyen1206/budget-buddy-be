package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.threshold.ThresholdDTO;
import com.budget.buddy.transaction.application.dto.threshold.ThresholdRequestDTO;
import com.budget.buddy.transaction.application.service.ThresholdService;
import com.budget.buddy.transaction.domain.service.ThresholdDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThresholdServiceImpl implements ThresholdService {

    private final ThresholdDataService thresholdDataService;

    @Override
    public ThresholdDTO create(ThresholdRequestDTO request) {
        return thresholdDataService.create(request.categoryId(), request.threshold(), request.currency());
    }

    @Override
    public ThresholdDTO view(Long id) {
        return thresholdDataService.view(id);
    }

    @Override
    public List<ThresholdDTO> viewAll() {
        return thresholdDataService.viewAll();
    }

    @Override
    public ThresholdDTO update(Long id, ThresholdRequestDTO request) {
        return thresholdDataService.update(id, request.categoryId(), request.threshold(), request.currency());
    }

    @Override
    public void delete(Long id) {
        thresholdDataService.delete(id);
    }
}

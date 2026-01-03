package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.saving.SavingDTO;
import com.budget.buddy.transaction.application.service.SavingService;
import com.budget.buddy.transaction.domain.service.SavingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingServiceImpl implements SavingService {

    private final SavingData savingData;

    @Override
    public void create(SavingDTO request) {
        savingData.create(request);
    }

    @Override
    public void update(Long id, SavingDTO request) {
        savingData.update(id, request);
    }

    @Override
    public void delete(Long id) {
        savingData.delete(id);
    }

    @Override
    public void deleteAll(Collection<Long> ids) {
        savingData.deleteAll(ids);
    }

    @Override
    public SavingDTO getById(Long id) {
        return savingData.getById(id);
    }

    @Override
    public List<SavingDTO> getAll() {
        return savingData.getAll();
    }
}

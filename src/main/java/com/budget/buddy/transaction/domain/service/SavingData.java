package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.saving.SavingDTO;

import java.util.Collection;
import java.util.List;

public interface SavingData {
    void create(SavingDTO request);

    void update(Long id, SavingDTO request);

    void delete(Long id);

    void deleteAll(Collection<Long> ids);

    SavingDTO getById(Long id);

    List<SavingDTO> getAll(String currency);
}

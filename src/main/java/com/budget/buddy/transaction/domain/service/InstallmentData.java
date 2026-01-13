package com.budget.buddy.transaction.domain.service;

import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;

import java.util.Collection;
import java.util.List;

public interface InstallmentData {
    void create(InstallmentDTO request);

    void update(Long id, InstallmentDTO request);

    void delete(Long id);

    void deleteAll(Collection<Long> ids);

    InstallmentDTO getById(Long id);

    List<InstallmentDTO> getAll();
}

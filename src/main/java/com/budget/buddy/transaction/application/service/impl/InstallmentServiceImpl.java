package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;
import com.budget.buddy.transaction.application.service.InstallmentService;
import com.budget.buddy.transaction.domain.service.InstallmentData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {

    private final InstallmentData installmentData;

    @Override
    public void create(InstallmentDTO request) {
        installmentData.create(request);
    }

    @Override
    public void update(Long id, InstallmentDTO request) {
        installmentData.update(id, request);
    }

    @Override
    public void delete(Long id) {
        installmentData.delete(id);
    }

    @Override
    public void deleteAll(Collection<Long> ids) {
        installmentData.deleteAll(ids);
    }

    @Override
    public InstallmentDTO getById(Long id) {
        return installmentData.getById(id);
    }

    @Override
    public List<InstallmentDTO> getAll() {
        return installmentData.getAll();
    }
}

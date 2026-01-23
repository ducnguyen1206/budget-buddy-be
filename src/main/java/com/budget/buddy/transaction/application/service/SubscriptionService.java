package com.budget.buddy.transaction.application.service;

import com.budget.buddy.transaction.application.dto.subscription.SubscriptionDTO;

import java.util.List;

public interface SubscriptionService {
    void create(SubscriptionDTO request);

    SubscriptionDTO update(Long id, SubscriptionDTO request);

    void delete(Long id);

    SubscriptionDTO getById(Long id);

    List<SubscriptionDTO> getAll();
}

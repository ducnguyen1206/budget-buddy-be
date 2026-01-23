package com.budget.buddy.transaction.application.service.impl;

import com.budget.buddy.transaction.application.dto.subscription.SubscriptionDTO;
import com.budget.buddy.transaction.application.service.SubscriptionService;
import com.budget.buddy.transaction.domain.service.SubscriptionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionData subscriptionData;

    @Override
    public void create(SubscriptionDTO request) {
        subscriptionData.create(request);
    }

    @Override
    public SubscriptionDTO update(Long id, SubscriptionDTO request) {
        return subscriptionData.update(id, request);
    }

    @Override
    public void delete(Long id) {
        subscriptionData.delete(id);
    }

    @Override
    public SubscriptionDTO getById(Long id) {
        return subscriptionData.getById(id);
    }

    @Override
    public List<SubscriptionDTO> getAll() {
        return subscriptionData.getAll();
    }
}

package com.budget.buddy.transaction.application.dto.account;

import java.math.BigDecimal;

public interface AccountFlatView {
    Long getId();

    String getName();

    BigDecimal getAmount();

    String getCurrency();

    String getGroupName();

    Long getGroupId();
}

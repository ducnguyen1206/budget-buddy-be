package com.budget.buddy.transaction.domain.vo;

import com.budget.buddy.transaction.domain.enums.Currency;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoneyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal amount;
    private String currency;

    // Constructor
    public MoneyVO(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency must not be null or empty");
        }

        this.amount = amount;
        this.currency = currency.name();
    }

    // Method to add two MoneyVO objects
    public MoneyVO add(MoneyVO other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies must match to perform addition");
        }
        return new MoneyVO(this.amount.add(other.amount), Currency.valueOf(this.currency));
    }

    // toString method for debugging
    @Override
    public String toString() {
        return amount + " " + currency;
    }

    // Equals and hashCode (for value equality)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoneyVO moneyVO)) return false;
        return amount.equals(moneyVO.amount) && currency.equals(moneyVO.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}

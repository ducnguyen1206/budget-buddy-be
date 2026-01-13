package com.budget.buddy.transaction.domain.model.installment;

import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "installment")
public class Installment extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    private Account account;

    @Column(nullable = false, length = 150)
    @NotBlank
    @Size(max = 150)
    private String name;

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    @NotNull
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 19, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amountPaid;

    @Column(name = "due_date", nullable = false)
    @NotNull
    private LocalDate dueDate;

    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(amountPaid);
    }
}

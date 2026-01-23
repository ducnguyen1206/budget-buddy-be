package com.budget.buddy.transaction.domain.model.subscription;

import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "subscription")
public class Subscription extends BaseEntity {

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

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "pay_day", nullable = false)
    @NotNull
    @Min(1)
    @Max(31)
    private Integer payDay;
}

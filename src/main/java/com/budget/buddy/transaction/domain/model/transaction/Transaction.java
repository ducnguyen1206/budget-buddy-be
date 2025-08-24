package com.budget.buddy.transaction.domain.model.transaction;

import com.budget.buddy.transaction.domain.model.account.Account;
import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import com.budget.buddy.transaction.domain.model.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(
    name = "transaction",
    indexes = {
        @Index(name = "idx_transaction_user_id", columnList = "user_id"),
        @Index(name = "idx_transaction_account_id", columnList = "account_id"),
        @Index(name = "idx_transaction_category_id", columnList = "category_id"),
    }
)
public class Transaction extends BaseEntity {
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;

    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    @NotNull
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    @NotNull
    @PastOrPresent
    private LocalDate date;

    @Column(nullable = false, length = 16)
    @NotBlank
    @Pattern(regexp = "INCOME|EXPENSE|TRANSFER", message = "type must be INCOME, EXPENSE, or TRANSFER")
    private String type;

    @Column(name = "transfer_info", nullable = false)
    @NotNull
    @Size(max = 255)
    private String transferInfo = "N/A";
}

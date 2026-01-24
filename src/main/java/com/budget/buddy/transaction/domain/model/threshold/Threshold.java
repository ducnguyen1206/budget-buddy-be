package com.budget.buddy.transaction.domain.model.threshold;

import com.budget.buddy.transaction.domain.enums.Currency;
import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import com.budget.buddy.transaction.domain.model.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "threshold")
public class Threshold extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;

    @Column(name = "threshold", precision = 19, scale = 2, nullable = false)
    @NotNull
    @PositiveOrZero
    private BigDecimal threshold;

    @Column(name = "currency", length = 3, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Currency currency;
}

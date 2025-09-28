package com.budget.buddy.transaction.domain.model.account;

import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import com.budget.buddy.transaction.domain.vo.MoneyVO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "account")
public class Account extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_type_group_id", nullable = false)
    @NotNull
    private AccountTypeGroup accountTypeGroup;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String name;

    @AttributeOverride(name = "amount", column = @Column(name = "available_balance", precision = 19, scale = 2, nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    @Embedded
    private MoneyVO money;
}

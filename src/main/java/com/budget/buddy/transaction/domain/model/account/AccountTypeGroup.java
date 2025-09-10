package com.budget.buddy.transaction.domain.model.account;

import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Entity
@Filter(name = "userFilter", condition = "user_id = :userId")
@Table(
        name = "account_type_group",
        indexes = {
                @Index(name = "idx_account_type_group_user_id", columnList = "user_id")
        }
)
public class AccountTypeGroup extends BaseEntity {
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String name;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "accountTypeGroup",
            orphanRemoval = true
    )
    private List<Account> accounts = new ArrayList<>();
}

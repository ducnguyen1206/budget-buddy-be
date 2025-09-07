package com.budget.buddy.transaction.domain.model.category;

import com.budget.buddy.transaction.domain.model.base.BaseEntity;
import com.budget.buddy.transaction.domain.vo.CategoryVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(
        name = "category"
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Filter(name = "userFilter", condition = "user_id = :userId")
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Category extends BaseEntity {

    @SuppressWarnings("java:S1948")
    @Embedded
    private CategoryVO identity;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;
}

package com.budget.buddy.user.domain.model;

import com.budget.buddy.core.BaseEntity;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@Accessors(fluent = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @SuppressWarnings("java:S1948")
    @Embedded
    private EmailAddressVO emailAddress;

    @Column
    private String password;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;
}

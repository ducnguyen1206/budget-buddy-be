package com.budget.buddy.user.domain.model;

import com.budget.buddy.core.BaseEntity;
import com.budget.buddy.user.domain.vo.EmailAddressVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Embedded
    private EmailAddressVO emailAddress;

    @Column
    private String password;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}

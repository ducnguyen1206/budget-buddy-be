package com.budget.buddy.user.domain.model;

import com.budget.buddy.user.domain.vo.VerificationTokenVO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_verification")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserVerification extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private User user;

    @SuppressWarnings("java:S1948")
    @Embedded
    private VerificationTokenVO verificationToken;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
}

package com.budget.buddy.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Embeddable
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationTokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "verification_token", nullable = false)
    private String value;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public VerificationTokenVO(String value, LocalDateTime expiresAt) {
        if (!isValidUUID(value) || Objects.isNull(expiresAt)) {
            throw new IllegalArgumentException("Token must not be blank");
        }

        this.value = value;
        this.expiresAt = expiresAt;
    }

    private boolean isValidUUID(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }

        try {
            UUID.fromString(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

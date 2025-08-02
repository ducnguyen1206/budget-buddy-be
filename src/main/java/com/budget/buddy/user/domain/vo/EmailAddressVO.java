package com.budget.buddy.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;


@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class EmailAddressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    // Only user for new account
    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    public EmailAddressVO(String value, boolean active) {
        final Pattern emailPattern =
                Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        if (value == null || !emailPattern.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.value = value.toLowerCase();
        this.active = active;
    }

    public EmailAddressVO activate() {
        return new EmailAddressVO(this.value, true);
    }

    public EmailAddressVO deactivate() {
        return new EmailAddressVO(this.value, false);
    }

    @Override
    public String toString() {
        return value;
    }
}

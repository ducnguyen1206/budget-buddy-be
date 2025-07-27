package com.budget.buddy.user.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;


@Embeddable
@Getter
@EqualsAndHashCode
public class EmailAddressVO {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    // Only user for new account
    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    protected EmailAddressVO() {
        // for JPA
    }

    public EmailAddressVO(String value, boolean active) {
        if (value == null || !EMAIL_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.value = value.toLowerCase();
        this.active = active;
    }

    public static EmailAddressVO inactive(String email) {
        return new EmailAddressVO(email, false);
    }

    public static EmailAddressVO active(String email) {
        return new EmailAddressVO(email, true);
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

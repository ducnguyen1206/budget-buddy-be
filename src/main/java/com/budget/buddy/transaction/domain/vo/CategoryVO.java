package com.budget.buddy.transaction.domain.vo;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryVO {
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    // Constructor
    public CategoryVO(String name, CategoryType type) {
        if (name == null || type == null) {
            throw new IllegalArgumentException("Amount and currency must not be null or empty");
        }

        this.name = name;
        this.type = type;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return name + " " + type;
    }

    // Equals and hashCode (for value equality)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryVO categoryVO)) return false;
        return name.equals(categoryVO.name) && type.equals(categoryVO.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}

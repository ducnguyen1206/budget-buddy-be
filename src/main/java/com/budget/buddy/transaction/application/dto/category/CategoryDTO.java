package com.budget.buddy.transaction.application.dto.category;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data Transfer Object for Category")
public record CategoryDTO(
        @Schema(description = "Unique identifier of the category", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Category name is required")
        @Schema(description = "Name of the category", example = "Food")
        String name,

        @NotNull(message = "Category type is required")
        @Schema(description = "Type of the category", example = "EXPENSE")
        CategoryType type
) {
}

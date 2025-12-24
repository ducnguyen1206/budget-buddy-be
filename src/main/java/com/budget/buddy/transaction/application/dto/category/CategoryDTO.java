package com.budget.buddy.transaction.application.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data Transfer Object for Category")
public record CategoryDTO(
        @Schema(description = "Unique identifier of the category", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Category name is required")
        @Schema(description = "Name of the category", example = "Food")
        String name
) {
}

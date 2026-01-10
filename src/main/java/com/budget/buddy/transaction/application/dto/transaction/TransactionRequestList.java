package com.budget.buddy.transaction.application.dto.transaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Transaction list details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestList {

    @Valid
    @Schema(description = "List of transaction details")
    @NotNull
    @Size(min = 1, message = "Request list cannot be empty")
    private List<@Valid TransactionDTO> requests;
}

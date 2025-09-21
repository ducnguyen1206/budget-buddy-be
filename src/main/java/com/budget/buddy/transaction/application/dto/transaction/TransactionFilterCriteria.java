package com.budget.buddy.transaction.application.dto.transaction;

import com.budget.buddy.transaction.domain.enums.CategoryType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "TransactionFilterCriteria", description = "Filters used to search transactions. All fields are optional. Combine fields to narrow results.")
public class TransactionFilterCriteria {
    @Valid
    @Schema(description = "Filter by transaction name (case-insensitive)", example = "{\n  \"operator\": \"contains\",\n  \"value\": \"grocery\"\n}")
    private StringFilter name;

    @Valid
    @Schema(description = "Filter by exact or comparative amount", example = "{\n  \"operator\": \">=\",\n  \"value\": 10.00\n}")
    private AmountFilter amount;

    @Schema(description = "Limit to transactions whose account currency is one of the given ISO-4217 codes (3 uppercase letters)", example = "[\"USD\", \"EUR\"]")
    private List<@Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO-4217 code (e.g., USD)") String> currencies;

    @Valid
    @Schema(description = "Filter by transaction date or date range", example = "{\n  \"operator\": \"is between\",\n  \"startDate\": \"2025-09-01\",\n  \"endDate\": \"2025-09-30\"\n}")
    private DateFilter date;

    @Valid
    @Schema(description = "Filter by source accounts using 'is' (IN) or 'is not' (NOT IN)", example = "{\n  \"operator\": \"is\",\n  \"ids\": [1,2,5]\n}")
    private IdsFilter accounts;

    @Valid
    @Schema(description = "Filter by categories using 'is' (IN) or 'is not' (NOT IN)", example = "{\n  \"operator\": \"is\",\n  \"ids\": [10,12]\n}")
    private IdsFilter categories;

    @Valid
    @Schema(description = "Filter by transaction types using 'is' (IN) or 'is not' (NOT IN)", example = "{\n  \"operator\": \"is\",\n  \"types\": [\"EXPENSE\", \"INCOME\"]\n}")
    private TypesFilter types;

    @Valid
    @Schema(description = "Filter by remarks (case-insensitive)", example = "{\n  \"operator\": \"does not contain\",\n  \"value\": \"reimbursed\"\n}")
    private StringFilter remarks;

    @Data
    @Schema(description = "Case-insensitive text filter with an operator and a value.")
    public static class StringFilter {
        @NotBlank
        @Pattern(regexp = "(?i)^(is|is not|contains|does not contain|starts with|ends with)$",
                message = "Operator must be one of: is, is not, contains, does not contain, starts with, ends with")
        @Schema(description = "Comparison operator for text fields", allowableValues = {"is", "is not", "contains", "does not contain", "starts with", "ends with"}, example = "contains")
        private String operator;

        @NotBlank
        @Size(max = 200, message = "Value length must be at most 200 characters")
        @Schema(description = "Text to compare against", example = "uber ride")
        private String value;
    }

    @Data
    @Schema(description = "Date filter. Use 'is' for a single day or 'is between' for a range (inclusive).")
    public static class DateFilter {
        @NotBlank
        @Pattern(regexp = "(?i)^(is|is between)$",
                message = "Operator must be one of: is, is between")
        @Schema(description = "Date operator", allowableValues = {"is", "is between"}, example = "is between")
        private String operator;

        @Schema(description = "Start date in ISO-8601 format (yyyy-MM-dd)", example = "2025-09-01")
        private LocalDate startDate;

        @Schema(description = "End date in ISO-8601 format (yyyy-MM-dd). Required for 'is between'", example = "2025-09-30")
        private LocalDate endDate;

        @AssertTrue(message = "For 'is', startDate is required. For 'is between', both startDate and endDate are required and startDate must be before or equal to endDate")
        public boolean isValidRange() {
            if (operator == null) return true; // handled by @NotBlank
            String op = operator.trim().toLowerCase();
            if ("is".equals(op)) {
                return startDate != null && endDate == null;
            }
            if ("is between".equals(op)) {
                return startDate != null && endDate != null && !endDate.isBefore(startDate);
            }
            return true; // regex validation will flag invalid operators
        }
    }

    @Data
    @Schema(description = "List-of-IDs filter with an operator for inclusion or exclusion.")
    public static class IdsFilter {
        @NotBlank
        @Pattern(regexp = "(?i)^(is|is not)$",
                message = "Operator must be one of: is, is not")
        @Schema(description = "Operator for list comparison", allowableValues = {"is", "is not"}, example = "is")
        private String operator;

        @Schema(description = "List of IDs", example = "[1,2,5]")
        private List<@Positive Long> ids;

        @AssertTrue(message = "At least one id is required when using the filter")
        public boolean hasIds() {
            if (operator == null) return true; // handled by @NotBlank
            return ids != null && !ids.isEmpty();
        }
    }

    @Data
    @Schema(description = "Types filter with inclusion or exclusion operator.")
    public static class TypesFilter {
        @NotBlank
        @Pattern(regexp = "(?i)^(is|is not)$",
                message = "Operator must be one of: is, is not")
        @Schema(description = "Operator for type comparison", allowableValues = {"is", "is not"}, example = "is")
        private String operator;

        @Schema(description = "List of transaction types", example = "[\"EXPENSE\",\"INCOME\"]")
        private List<CategoryType> types;

        @AssertTrue(message = "At least one type is required when using the filter")
        public boolean hasTypes() {
            if (operator == null) return true; // handled by @NotBlank
            return types != null && !types.isEmpty();
        }
    }

    @Data
    @Schema(description = "Amount filter supporting equality and inequality operators.")
    public static class AmountFilter {
        @NotBlank
        @Pattern(regexp = "^(=|!=|>|<|>=|<=)$",
                message = "Operator must be one of: =, !=, >, <, >=, <=")
        @Schema(description = "Numeric comparison operator", allowableValues = {"=", "!=", ">", "<", ">=", "<="}, example = ">=")
        private String operator;

        @Schema(description = "Amount value to compare against", example = "25.50")
        private BigDecimal value;

        @AssertTrue(message = "Amount value is required when amount filter is provided")
        public boolean hasValue() {
            if (operator == null) return true; // handled by @NotBlank
            return value != null;
        }
    }
}

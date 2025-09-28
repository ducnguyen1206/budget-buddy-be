package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.budget.BudgetDTO;
import com.budget.buddy.transaction.application.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Budget Management", description = "CRUD APIs for managing budget management")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(summary = "Create a new budget", description = "Creates a new budget for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Category not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        budgetService.saveBudget(budgetDTO);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Update a existed budget", description = "Update a existed for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Budget not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBudget(@Valid @RequestBody BudgetDTO budgetDTO, @PathVariable("id") Long id) {
        budgetService.updateBudget(budgetDTO, id);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Delete a existed budget", description = "Delete a existed for the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Budget not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable("id") Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.status(204).build();
    }

    @Operation(summary = "List my budgets", description = "Returns all budgets created by the authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BudgetDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets() {
        return ResponseEntity.status(200).body(budgetService.getAllBudgetsForCurrentUser());
    }

    @Operation(summary = "Get a budget by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Budget retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BudgetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Budget not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBudget(@PathVariable("id") Long id) {
        return ResponseEntity.status(200).body(budgetService.getBudgetById(id));
    }
}

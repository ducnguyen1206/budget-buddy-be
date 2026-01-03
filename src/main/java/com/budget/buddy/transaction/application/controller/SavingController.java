package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.saving.SavingDTO;
import com.budget.buddy.transaction.application.service.SavingService;
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

import java.util.Collection;
import java.util.List;

@Tag(name = "Saving Management", description = "CRUD APIs for managing savings goals linked to user accounts")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/savings")
public class SavingController {

    private final SavingService savingService;

    @Operation(summary = "Create a new saving goal", responses = {
            @ApiResponse(responseCode = "201", description = "Saving created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody SavingDTO request) {
        savingService.create(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Update a saving goal", responses = {
            @ApiResponse(responseCode = "204", description = "Saving updated successfully"),
            @ApiResponse(responseCode = "404", description = "Saving not found", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody SavingDTO request) {
        savingService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a saving goal", responses = {
            @ApiResponse(responseCode = "204", description = "Saving deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Saving not found", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        savingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk delete saving goals by IDs", responses = {
            @ApiResponse(responseCode = "204", description = "Savings deleted successfully")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestBody Collection<Long> ids) {
        savingService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all savings for current user", responses = {
            @ApiResponse(responseCode = "200", description = "Savings retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SavingDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<SavingDTO>> getAll(@RequestParam(required = false) String currency) {
        return ResponseEntity.ok(savingService.getAll(currency));
    }

    @Operation(summary = "Get a saving by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Saving retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SavingDTO.class))),
            @ApiResponse(responseCode = "404", description = "Saving not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<SavingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(savingService.getById(id));
    }
}

package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.installment.InstallmentDTO;
import com.budget.buddy.transaction.application.service.InstallmentService;
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

@Tag(name = "Installment Management", description = "CRUD APIs for managing installment payments linked to user accounts")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/installments")
public class InstallmentController {

    private final InstallmentService installmentService;

    @Operation(summary = "Create a new installment", responses = {
            @ApiResponse(responseCode = "201", description = "Installment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody InstallmentDTO request) {
        installmentService.create(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Update an installment", responses = {
            @ApiResponse(responseCode = "204", description = "Installment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Installment not found", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody InstallmentDTO request) {
        installmentService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete an installment", responses = {
            @ApiResponse(responseCode = "204", description = "Installment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Installment not found", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        installmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bulk delete installments by IDs", responses = {
            @ApiResponse(responseCode = "204", description = "Installments deleted successfully")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestBody Collection<Long> ids) {
        installmentService.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all installments for current user", responses = {
            @ApiResponse(responseCode = "200", description = "Installments retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = InstallmentDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<InstallmentDTO>> getAll() {
        return ResponseEntity.ok(installmentService.getAll());
    }

    @Operation(summary = "Get an installment by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Installment retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InstallmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Installment not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<InstallmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(installmentService.getById(id));
    }
}

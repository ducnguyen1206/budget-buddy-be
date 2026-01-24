package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.threshold.ThresholdDTO;
import com.budget.buddy.transaction.application.dto.threshold.ThresholdRequestDTO;
import com.budget.buddy.transaction.application.service.ThresholdService;
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

@Tag(name = "Threshold Management", description = "CRUD APIs for managing spending thresholds per category")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/thresholds")
public class ThresholdController {

    private final ThresholdService thresholdService;

    @Operation(summary = "Create a new threshold", responses = {
            @ApiResponse(responseCode = "201", description = "Threshold created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ThresholdDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<ThresholdDTO> create(@Valid @RequestBody ThresholdRequestDTO request) {
        ThresholdDTO response = thresholdService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Get a threshold by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Threshold retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ThresholdDTO.class))),
            @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<ThresholdDTO> view(@PathVariable Long id) {
        return ResponseEntity.ok(thresholdService.view(id));
    }

    @Operation(summary = "Get all thresholds for current user", responses = {
            @ApiResponse(responseCode = "200", description = "Thresholds retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ThresholdDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<ThresholdDTO>> viewAll() {
        return ResponseEntity.ok(thresholdService.viewAll());
    }

    @Operation(summary = "Update a threshold", responses = {
            @ApiResponse(responseCode = "200", description = "Threshold updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ThresholdDTO.class))),
            @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<ThresholdDTO> update(@PathVariable Long id, @Valid @RequestBody ThresholdRequestDTO request) {
        return ResponseEntity.ok(thresholdService.update(id, request));
    }

    @Operation(summary = "Delete a threshold", responses = {
            @ApiResponse(responseCode = "204", description = "Threshold deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Threshold not found", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        thresholdService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

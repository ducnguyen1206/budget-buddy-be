package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.subscription.SubscriptionDTO;
import com.budget.buddy.transaction.application.service.SubscriptionService;
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

@Tag(name = "Subscription Management", description = "CRUD APIs for managing recurring subscriptions linked to user accounts")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Create a new subscription", responses = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody SubscriptionDTO request) {
        subscriptionService.create(request);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Update a subscription", responses = {
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SubscriptionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> update(@PathVariable Long id, @Valid @RequestBody SubscriptionDTO request) {
        return ResponseEntity.ok(subscriptionService.update(id, request));
    }

    @Operation(summary = "Delete a subscription", responses = {
            @ApiResponse(responseCode = "204", description = "Subscription deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all subscriptions for current user", responses = {
            @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SubscriptionDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAll());
    }

    @Operation(summary = "Get a subscription by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Subscription retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SubscriptionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getById(id));
    }
}

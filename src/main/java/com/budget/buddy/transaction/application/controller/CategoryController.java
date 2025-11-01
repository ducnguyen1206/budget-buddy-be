package com.budget.buddy.transaction.application.controller;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.application.service.CategoryService;
import com.budget.buddy.transaction.domain.enums.CategoryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category Management", description = "CRUD APIs for managing transaction categories")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", description = "Creates a category for the authenticated user.", responses = {
            @ApiResponse(responseCode = "201", description = "Category added successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @Operation(summary = "Update a category", description = "Updates the specified category if it belongs to the authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody CategoryDTO request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Get a category by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @Operation(summary = "List my categories", description = "Returns all categories created by the authenticated user.", responses = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getMyCategories(@RequestParam(required = false) CategoryType type) {
        return ResponseEntity.ok(categoryService.getMyCategories(type));
    }

    @Operation(summary = "Delete a category by ID", description = "Deletes the specified category if it belongs to the authenticated user.", responses = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content())
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}

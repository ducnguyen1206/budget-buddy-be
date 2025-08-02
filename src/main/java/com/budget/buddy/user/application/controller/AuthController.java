package com.budget.buddy.user.application.controller;

import com.budget.buddy.user.application.dto.RefreshTokenRequest;
import com.budget.buddy.user.application.dto.RegisterRequest;
import com.budget.buddy.user.application.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", responses = {
            @ApiResponse(responseCode = "201", description = "User registered and logged in, email is sent to user"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.registerUser(request.email());
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Verify user email", responses = {
            @ApiResponse(responseCode = "200", description = "AccountPayload verified"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Token invalid")
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody RefreshTokenRequest tokenRequest) {
        authenticationService.verifyUser(tokenRequest.refreshToken());
        return ResponseEntity.ok("AccountPayload verified");
    }
}

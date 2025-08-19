package com.budget.buddy.user.application.controller;

import com.budget.buddy.user.application.dto.*;
import com.budget.buddy.user.application.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Endpoints for user authentication")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", responses = {
            @ApiResponse(responseCode = "201", description = "User registered and logged in, email is sent to user"),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content())
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.registerUser(request.email());
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Verify user email", responses = {
            @ApiResponse(responseCode = "200", description = "AccountPayload verified"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Token invalid", content = @Content())
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody RefreshTokenRequest tokenRequest) {
        authenticationService.verifyUser(tokenRequest.refreshToken());
        return ResponseEntity.ok("AccountPayload verified");
    }

    @Operation(
            summary = "Login with email and password",
            description = "Authenticates a user using email and password. Returns an access token and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authenticationService.login(request.email(), request.password())
        );
    }

    @Operation(
            summary = "Refresh the access token",
            description = "Generates a new access token and refresh token using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content())
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                authenticationService.refreshToken(request.refreshToken())
        );
    }

    @Operation(
            summary = "Logout the user",
            description = "Deletes the active session for the user, invalidating tokens.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logout successful"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String email) {
        authenticationService.logout(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reset user password",
            description = "Resets the user's password to a new one. May require verification steps depending on business rules.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password reset successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}

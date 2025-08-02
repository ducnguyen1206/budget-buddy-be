package com.budget.buddy.user.application.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class UserExceptionHandler {
    private static final Logger logger = LogManager.getLogger(UserExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<UserErrorResponse> handleAuthException(AuthException ex) {
        logger.warn("Authentication error: {} - {}", ex.getErrorCode(), ex.getMessage());
        UserErrorResponse error = new UserErrorResponse(ex.getErrorCode(), ex.getMessage());
        Map<String, HttpStatus> statusCode = new HashMap<>();
        statusCode.put(UserErrorCode.ACCOUNT_LOCKED.getCode(), HttpStatus.FORBIDDEN);
        statusCode.put(UserErrorCode.EMAIL_EXISTS.getCode(), HttpStatus.CONFLICT);

        HttpStatus status = statusCode.getOrDefault(ex.getErrorCode(), HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(status).body(error);
    }
}

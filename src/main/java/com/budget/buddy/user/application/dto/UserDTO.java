package com.budget.buddy.user.application.dto;

import java.time.LocalDateTime;

public record UserDTO(Long id,
                      String email,
                      boolean active,
                      boolean locked,
                      LocalDateTime createdDate,
                      LocalDateTime lastModifiedDate) {
}

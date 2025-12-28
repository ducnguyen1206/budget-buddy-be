package com.budget.buddy.user.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
        @JsonProperty("sub") String id,
        @JsonProperty("name") String name,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName,
        @JsonProperty("picture") String picture,
        @JsonProperty("email") String email,
        @JsonProperty("email_verified") boolean emailVerified,
        @JsonProperty("locale") String locale
) {}

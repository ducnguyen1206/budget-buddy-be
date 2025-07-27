package com.budget.buddy.user.application.mapper;

import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "emailAddress.value", target = "email")
    @Mapping(source = "emailAddress.active", target = "active")
    UserDTO toDto(User user);
}

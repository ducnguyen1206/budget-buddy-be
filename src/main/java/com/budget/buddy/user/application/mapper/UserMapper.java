package com.budget.buddy.user.application.mapper;

import com.budget.buddy.user.application.dto.UserDTO;
import com.budget.buddy.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "email", expression = "java(user.getEmailAddress().getValue())")
    @Mapping(target = "active", expression = "java(user.getEmailAddress().isActive())")
    UserDTO toDto(User user);
}

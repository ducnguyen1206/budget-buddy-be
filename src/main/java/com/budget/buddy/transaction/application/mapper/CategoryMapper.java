package com.budget.buddy.transaction.application.mapper;

import com.budget.buddy.transaction.application.dto.category.CategoryDTO;
import com.budget.buddy.transaction.domain.model.category.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    @Mapping(target = "id", expression = "java(category.getId())")
    @Mapping(target = "name", expression = "java(category.getIdentity().getName())")
    CategoryDTO toDto(Category category);


    List<CategoryDTO> toDtoList(List<Category> categories);

}

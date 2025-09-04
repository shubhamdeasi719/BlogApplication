package com.blog_application.blogApp.service;

import com.blog_application.blogApp.payloads.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(CategoryDto categoryDto);
    CategoryDto getCategoryById(Integer id);
    List<CategoryDto> getAllCategories();
    void deleteCategory(Integer id);
}

package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.exceptionHandler.CategoryNotFoundException;
import com.blog_application.blogApp.payloads.CategoryDto;
import com.blog_application.blogApp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository  categoryRepository, ModelMapper modelMapper)
    {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = dtoToEntity(categoryDto);
        Category newCategory = categoryRepository.save(category);
        return entityToDto(newCategory);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        Category category= dtoToEntity(categoryDto);
        Optional<Category> optionalCategory = categoryRepository.findById(category.getId());
        if(optionalCategory.isEmpty())
        {
            throw new CategoryNotFoundException("No Category found with id: "+category.getId());
        }

        Category updatedCategory = optionalCategory.get();
        updatedCategory.setCategoryTitle(category.getCategoryTitle());
        updatedCategory.setCategoryDescription(category.getCategoryDescription());
        return entityToDto(categoryRepository.save(updatedCategory));

    }

    @Override
    public CategoryDto getCategoryById(Integer id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isEmpty())
        {
            throw new CategoryNotFoundException("No category found with id: "+id);
        }

        Category existingCategory = optionalCategory.get();
        return entityToDto(existingCategory);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        List<CategoryDto> categoryDtoList = categoryList.stream().map(category -> entityToDto(category)).toList();
        return categoryDtoList;
    }

    @Override
    public void deleteCategory(Integer id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isEmpty())
        {
            throw  new CategoryNotFoundException("No category found with id: "+id);
        }

        Category category = optionalCategory.get();
        categoryRepository.delete(category);
    }

    public CategoryDto entityToDto(Category category)
    {
        CategoryDto  categoryDto = modelMapper.map(category,CategoryDto.class);
        return categoryDto;
    }

    public Category dtoToEntity(CategoryDto categoryDto)
    {
        Category category = modelMapper.map(categoryDto,Category.class);
        return category;
    }
}

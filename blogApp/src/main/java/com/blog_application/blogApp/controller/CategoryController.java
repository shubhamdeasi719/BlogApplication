package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.CategoryDto;
import com.blog_application.blogApp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService  categoryService)
    {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-category")
    public ResponseEntity<CategoryDto> createCategory(@Valid  @RequestBody CategoryDto categoryDto)
    {
        CategoryDto newCategoryDto = categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(newCategoryDto, HttpStatus.CREATED);
    }

    @GetMapping("/all-categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories()
    {
        List<CategoryDto> categoryDtoList = categoryService.getAllCategories();
        return new ResponseEntity<>(categoryDtoList,HttpStatus.FOUND);
    }

    @GetMapping("/one-category")
    public ResponseEntity<CategoryDto> getCategoryById(@RequestParam Integer id)
    {
        CategoryDto categoryDto = categoryService.getCategoryById(id);
        return new ResponseEntity<>(categoryDto,HttpStatus.FOUND);
    }

    @PutMapping("/update-category")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto)
    {
        CategoryDto updatedCategory = categoryService.updateCategory(categoryDto);
        return new ResponseEntity<>(updatedCategory,HttpStatus.OK);
    }

    @DeleteMapping("/delete-category/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer id)
    {
        categoryService.deleteCategory(id);
        ApiResponse apiResponse =  new ApiResponse("Category Deleted Successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}

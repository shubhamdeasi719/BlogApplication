package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.CategoryDto;
import com.blog_application.blogApp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name="Category APIs", description = "Create - Read - Update - Delete Categories")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService  categoryService)
    {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add-category")
    @Operation(summary = "Create New Category, Only Admin Can Have Access")
    public ResponseEntity<CategoryDto> createCategory(@Valid  @RequestBody CategoryDto categoryDto)
    {
        CategoryDto newCategoryDto = categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(newCategoryDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/all-categories")
    @Operation(summary = "Get All Categories, Both Admin and User Can Have Access")
    public ResponseEntity<List<CategoryDto>> getAllCategories()
    {
        List<CategoryDto> categoryDtoList = categoryService.getAllCategories();
        return new ResponseEntity<>(categoryDtoList,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/one-category")
    @Operation(summary = "Get Single Category By Id , Both Admin and User Can Have Access")
    public ResponseEntity<CategoryDto> getCategoryById(@RequestParam Integer id)
    {
        CategoryDto categoryDto = categoryService.getCategoryById(id);
        return new ResponseEntity<>(categoryDto,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-category")
    @Operation(summary = "Update Category, Only Admin Can Have Access")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto)
    {
        CategoryDto updatedCategory = categoryService.updateCategory(categoryDto);
        return new ResponseEntity<>(updatedCategory,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-category/{id}")
    @Operation(summary = "Delete Category, Only Admin Can Have Access")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer id)
    {
        categoryService.deleteCategory(id);
        ApiResponse apiResponse =  new ApiResponse("Category Deleted Successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}

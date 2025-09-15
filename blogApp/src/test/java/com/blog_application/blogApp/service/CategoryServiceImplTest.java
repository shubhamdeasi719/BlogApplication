package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.exceptionHandler.CategoryNotFoundException;
import com.blog_application.blogApp.payloads.CategoryDto;
import com.blog_application.blogApp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    CategoryServiceImpl categoryServiceImpl;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp()
    {
        category = new Category(1,"AI/ML","Currently AI/ML is in trending, It means AI/ML is not future,It is already comes into picture",null);
        categoryDto =  new CategoryDto(1,"AI/ML","Currently AI/ML is in trending, It means AI/ML is not future,It is already comes into picture");
    }

    @Test
    void testCreateCategory_Success()
    {
        when(modelMapper.map(categoryDto, Category.class)).thenReturn(category);

        when(categoryRepository.save(category)).thenReturn(category);

        when(modelMapper.map(category, CategoryDto.class)).thenReturn(categoryDto);

        CategoryDto result = categoryServiceImpl.createCategory(categoryDto);

        assertNotNull(result);
        assertEquals(categoryDto.getCategoryTitle(), result.getCategoryTitle());
        assertEquals(categoryDto.getCategoryDescription(), result.getCategoryDescription());

        verify(modelMapper, times(1)).map(categoryDto,Category.class);
        verify(categoryRepository,times(1)).save(category);
        verify(modelMapper, times(1)).map(category, CategoryDto.class);
    }

    @Test
    void testUpdateCategory_Success()
    {
        Category updateCategory = new Category(1,"Updated AI/ML","Update description",null);
        CategoryDto updatedCategoryDto = new CategoryDto(1,"Updated AI/ML","Updated description");

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(modelMapper.map(updatedCategoryDto, Category.class)).thenReturn(updateCategory);

        when(categoryRepository.save(updateCategory)).thenReturn(updateCategory);
        when(modelMapper.map(updateCategory, CategoryDto.class)).thenReturn(updatedCategoryDto);

        CategoryDto result = categoryServiceImpl.updateCategory(updatedCategoryDto);

        assertNotNull(result);
        assertEquals(updatedCategoryDto.getCategoryTitle(), result.getCategoryTitle());
        assertEquals(updatedCategoryDto.getCategoryDescription(), result.getCategoryDescription());

        verify(categoryRepository,times(1)).findById(category.getId());
        verify(categoryRepository, times(1)).save(updateCategory);

    }

    @Test
    void testUpdateCategory_NotFound()
    {
        CategoryDto nonExistentCategoryDto = new CategoryDto(999, "Non-existent","Description");

        Category nonExistentCategory = new Category();
        nonExistentCategory.setId(nonExistentCategoryDto.getId());

        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(modelMapper.map(nonExistentCategoryDto, Category.class)).thenReturn(nonExistentCategory);

        assertThrows(CategoryNotFoundException.class, ()-> categoryServiceImpl.updateCategory(nonExistentCategoryDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testGetCategoryById_Success()
    {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDto.class)).thenReturn(categoryDto);

        CategoryDto result = categoryServiceImpl.getCategoryById(category.getId());

        assertNotNull(result);
        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getCategoryTitle(), result.getCategoryTitle());

        verify(categoryRepository, times(1)).findById(category.getId());
        verify(modelMapper, times(1)).map(category, CategoryDto.class);
    }

    @Test
    void testGetCategoryById_NotFound()
    {
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, ()-> categoryServiceImpl.getCategoryById(999));
    }

    @Test
    void testGetAllCategories_Success()
    {
        List<Category> categoryList = Arrays.asList(category, new Category(2,"Another Category","Another category description",null));
        List<CategoryDto> categoryDtoList = Arrays.asList(categoryDto, new CategoryDto(2,"Another Category","Another category description"));

        when(categoryRepository.findAll()).thenReturn(categoryList);

        when(modelMapper.map(any(Category.class), eq(CategoryDto.class))).thenReturn(categoryDto, new CategoryDto(2,"Another Category","Another category description"));

        List<CategoryDto> result = categoryServiceImpl.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(categoryDtoList.get(0).getCategoryTitle(), result.get(0).getCategoryTitle());
        assertEquals(categoryDtoList.get(1).getCategoryTitle(), result.get(1).getCategoryTitle());

        verify(categoryRepository, times(1)).findAll();
        verify(modelMapper, times(2)).map(any(Category.class), eq(CategoryDto.class));
    }

    @Test
    void testGetAllCategories_EmptyList()
    {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        List<CategoryDto> result = categoryServiceImpl.getAllCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testDeleteCategory_Success()
    {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        assertDoesNotThrow(()-> categoryServiceImpl.deleteCategory(category.getId()));

        verify(categoryRepository,  times(1)).delete(category);
    }

    @Test
    void testDeleteCategory_NotFound()
    {
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()->categoryServiceImpl.deleteCategory(999));

        verify(categoryRepository,  never()).delete(any(Category.class));
    }
}

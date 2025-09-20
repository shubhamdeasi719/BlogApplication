package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.config.SecurityConfig;
import com.blog_application.blogApp.exceptionHandler.CategoryNotFoundException;
import com.blog_application.blogApp.payloads.CategoryDto;
import com.blog_application.blogApp.security.CustomUserDetailService;
import com.blog_application.blogApp.security.JwtTokenHelper;
import com.blog_application.blogApp.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.With;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
public class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    @MockitoBean
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto categoryDto;

    @BeforeEach
    void setUp()
    {
        categoryDto = new CategoryDto();
        categoryDto.setId(1);
        categoryDto.setCategoryTitle("Test category");
        categoryDto.setCategoryDescription("A description for test category");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateCategory_Success_asAdmin() throws Exception
    {
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/api/categories/add-category")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryTitle").value(categoryDto.getCategoryTitle()));

        verify(categoryService, times(1)).createCategory(any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreateCategory_Forbidden_asUser() throws Exception
    {
        mockMvc.perform(post("/api/categories/add-category")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetAllCategories_Success_asUser() throws  Exception
    {
        List<CategoryDto> categoryDtoList = Collections.singletonList(categoryDto);
        when(categoryService.getAllCategories()).thenReturn(categoryDtoList);

        mockMvc.perform(get("/api/categories/all-categories")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryTitle").value(categoryDto.getCategoryTitle()));
        verify(categoryService,times(1)).getAllCategories();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetCategoryByIdd_Success_asAdmin() throws Exception
    {
        when(categoryService.getCategoryById(anyInt())).thenReturn(categoryDto);

        mockMvc.perform(get("/api/categories/one-category")
                .with(csrf())
                .param("id", String.valueOf(1)))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.categoryTitle").value(categoryDto.getCategoryTitle()));

        verify(categoryService, times(1)).getCategoryById(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetCategoryById_NotFound() throws Exception
    {
        when(categoryService.getCategoryById(anyInt())).thenThrow(new CategoryNotFoundException("Category not found"));

        mockMvc.perform(get("/api/categories/one-category")
                .with(csrf())
                .param("id",String.valueOf("100")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateCategory_Success_asAdmin() throws Exception
    {
        when(categoryService.updateCategory(any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(put("/api/categories/update-category")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryTitle").value(categoryDto.getCategoryTitle()));

        verify(categoryService, times(1)).updateCategory(any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testUpdateCategory_Forbidden_asUser() throws Exception
    {
        mockMvc.perform(put("/api/categories/update-category")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteCategory_Success_asAdmin() throws Exception
    {
        doNothing().when(categoryService).deleteCategory(anyInt());

        mockMvc.perform(delete("/api/categories/delete-category/{id}",1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category Deleted Successfully"));
        verify(categoryService, times(1)).deleteCategory(1);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testDeleteCategory_Forbidden_asUser() throws Exception
    {
        mockMvc.perform(delete("/api/categories/delete-category/{id}",1)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}

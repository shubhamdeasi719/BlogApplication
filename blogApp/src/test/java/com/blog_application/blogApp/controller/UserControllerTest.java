package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private UserDto updatedUserDto;

    @TestConfiguration
    @EnableMethodSecurity
    public static class TestSecurityConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Shubham");
        userDto.setEmail("shub@gmail.com");
        userDto.setPassword("shub@123");
        userDto.setAbout("He is java developer");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateUser_Success_asAdmin() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/users/create-user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "user" , roles = {"USER"})
    void testCreateUser_Forbidden_asUser() throws Exception
    {
        mockMvc.perform(post("/api/users/create-user")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUser_Success_asAdmin() throws Exception {
        List<UserDto> userDtoList = Collections.singletonList(userDto);
        when(userService.getAllUsers()).thenReturn(userDtoList);

        mockMvc.perform(get("/api/users/all-users")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].name").value(userDto.getName()));
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(username = "user", roles={"USER"})
    void testGetAllUsers_Forbidden_asUser() throws Exception{
        mockMvc.perform(get("/api/users/all-users")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUpdateUser_Success_asUser() throws Exception
    {
        when(userService.updateUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(put("/api/users/update-user")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userDto.getName()));

        verify(userService, times(1)).updateUser(any(UserDto.class));

    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUpdateUser_InvalidInput() throws Exception {
        userDto.setEmail("invalid-email");
        mockMvc.perform(put("/api/users/update-user")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin",roles = {"ADMIN"})
    void testDeleteUser_Success_asAdmin() throws Exception
    {
        doNothing().when(userService).deleteUser(anyInt());

        mockMvc.perform(delete("/api/users/delete-user/{id}", 1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    @WithMockUser(username = "user", roles = {"User"})
    void testDeleteUser_Forbidden_asUser() throws Exception
    {
        mockMvc.perform(delete("/api/users/delete-user/{id}",1)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}

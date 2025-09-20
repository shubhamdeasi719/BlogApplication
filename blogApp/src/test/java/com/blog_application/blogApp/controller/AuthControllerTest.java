package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.JwtAuthRequest;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.security.CustomUserDetailService;
import com.blog_application.blogApp.security.JwtTokenHelper;
import com.blog_application.blogApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenHelper jwtTokenHelper;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private JwtAuthRequest jwtAuthRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtAuthRequest = new JwtAuthRequest("abc@gmail.com", "abc@123");
        userDetails = new User(jwtAuthRequest.getEmail(), jwtAuthRequest.getPassword(), Collections.emptyList());
    }

    @Test
    void testCreateToken_Success() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(customUserDetailService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtTokenHelper.generateToken(anyString())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void testCreateToken_InvalidCredentials() throws Exception
    {
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Invalid Credentials"));

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jwtAuthRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterUser_Success() throws Exception
    {
        UserDto newUserDto = new UserDto();
        newUserDto.setName("Test user");
        newUserDto.setEmail("test@example.com");
        newUserDto.setPassword("pass@123");
        newUserDto.setAbout("Test about");

        when(userService.registerUser(any(UserDto.class))).thenReturn(newUserDto);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(newUserDto.getName()))
                .andExpect(jsonPath("$.email").value(newUserDto.getEmail()));
    }
}

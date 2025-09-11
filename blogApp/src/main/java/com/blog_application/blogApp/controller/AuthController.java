package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.JwtAuthRequest;
import com.blog_application.blogApp.payloads.JwtAuthResponse;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.security.CustomUserDetailService;
import com.blog_application.blogApp.security.JwtTokenHelper;
import com.blog_application.blogApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name="Authentication APIs", description = "Login and Register New User")
public class AuthController {

    private AuthenticationManager  authenticationManager;
    private JwtTokenHelper jwtTokenHelper;
    private CustomUserDetailService customUserDetailService;
    private UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenHelper jwtTokenHelper, CustomUserDetailService customUserDetailService, UserService userService)
    {
        this.authenticationManager = authenticationManager;
        this.jwtTokenHelper = jwtTokenHelper;
        this.customUserDetailService = customUserDetailService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login, Everyone Can Access")
    public ResponseEntity<JwtAuthResponse> createToken(@Valid @RequestBody JwtAuthRequest request)
    {
        authenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = customUserDetailService.loadUserByUsername(request.getEmail());

        String token = jwtTokenHelper.generateToken(userDetails.getUsername());

        return new ResponseEntity<>(new JwtAuthResponse(token), HttpStatus.OK);
    }

    private void authenticate(String email, String password) {
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @PostMapping("/register")
    @Operation(summary = "Register New User, Everyone Can Access")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        UserDto newUser = userService.registerUser(userDto);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}

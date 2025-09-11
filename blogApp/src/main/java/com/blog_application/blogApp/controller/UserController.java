package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name="User APIs", description = "Create - Read - Update - Delete Users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    @Operation(summary = "Create New User, Only Admin Can Have Access")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto)
    {
        UserDto newUserDto = userService.createUser(userDto);
        return new ResponseEntity<>(newUserDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-users")
    @Operation(summary = "Get All User, Only Admin Can Have Access")
    public ResponseEntity<List<UserDto>> getAllUsers()
    {
        List<UserDto> userDtoList = userService.getAllUsers();
        return new ResponseEntity<>(userDtoList,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/one-user")
    @Operation(summary = "Get single User, Only Admin Can Have Access")
    public  ResponseEntity<UserDto> getUserById(@RequestParam Integer id)
    {
        UserDto userDto = userService.getUserById(id);
        return new ResponseEntity<>(userDto,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping("/update-user")
    @Operation(summary = "Update User, Only Admin and Owner Can Have Access")
    public ResponseEntity<UserDto> updateUser(@Valid  @RequestBody UserDto userDto)
    {
        UserDto updatedUser = userService.updateUser(userDto);
        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-user/{id}")
    @Operation(summary = "Delete User, Only Admin Can Have Access")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer id)
    {
        userService.deleteUser(id);
        ApiResponse apiResponse = new ApiResponse("User deleted successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}

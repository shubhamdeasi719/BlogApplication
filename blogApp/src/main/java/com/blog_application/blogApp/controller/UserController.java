package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto)
    {
        UserDto newUserDto = userService.createUser(userDto);
        return new ResponseEntity<>(newUserDto, HttpStatus.CREATED);
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<UserDto>> getAllUsers()
    {
        List<UserDto> userDtoList = userService.getAllUsers();
        return new ResponseEntity<>(userDtoList,HttpStatus.FOUND);
    }

    @GetMapping("/one-user")
    public  ResponseEntity<UserDto> getUserById(@RequestParam Integer id)
    {
        UserDto userDto = userService.getUserById(id);
        return new ResponseEntity<>(userDto,HttpStatus.FOUND);
    }

    @PutMapping("/update-user")
    public ResponseEntity<UserDto> updateUser(@Valid  @RequestBody UserDto userDto)
    {
        UserDto updatedUser = userService.updateUser(userDto);
        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Integer id)
    {
        userService.deleteUser(id);
        ApiResponse apiResponse = new ApiResponse("User deleted successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}

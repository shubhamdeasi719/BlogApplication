package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.payloads.UserDto;

import java.util.List;

public interface UserService {

     UserDto createUser(UserDto userDto);
     UserDto updateUser(UserDto userDto);
     UserDto getUserById(Integer id);
     List<UserDto> getAllUsers();
     void deleteUser(Integer id);
}

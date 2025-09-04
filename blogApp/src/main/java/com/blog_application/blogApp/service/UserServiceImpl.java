package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper)
    {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = dtoToEntity(userDto);
        User newUser =  userRepository.save(user);
        return entityToDto(newUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = dtoToEntity(userDto);
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+user.getId());
        }

        User existingUser = optionalUser.get();
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setAbout(user.getAbout());

        User updatedUser = userRepository.save(existingUser);
        return entityToDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+id);
        }

        User existingUser = optionalUser.get();
        return entityToDto(existingUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
       List<User> userList = userRepository.findAll();
       List<UserDto> userDtoList = userList.stream().map(user -> entityToDto(user)).toList();
       return userDtoList;
    }

    @Override
    public void deleteUser(Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User Not found wit id: "+id);
        }
        User user = optionalUser.get();
        userRepository.delete(user);
    }

    public UserDto entityToDto(User user)
    {
        UserDto userDto = modelMapper.map(user,UserDto.class);
        return userDto;
    }

    public User dtoToEntity(UserDto userDto)
    {
        User user = modelMapper.map(userDto,User.class);
        return user;
    }
}

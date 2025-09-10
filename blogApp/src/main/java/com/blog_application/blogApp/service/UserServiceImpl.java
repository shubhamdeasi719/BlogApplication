package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Role;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.RoleNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UnAuthorizedException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.repository.RoleRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ModelMapper modelMapper)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto registerUser(UserDto userDto) {
        User user = dtoToEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role defaultRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RoleNotFoundException("Default role not found"));
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);
        return entityToDto(savedUser);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = dtoToEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName(userDto.getRoleName()).orElseThrow(() -> new RoleNotFoundException("Role not found"));
        user.setRole(role);

        User newUser =  userRepository.save(user);
        return entityToDto(newUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Logged-in user not found"+currentUsername));

        User user = dtoToEntity(userDto);
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+user.getId());
        }

        User existingUser = optionalUser.get();

        if (!currentUser.getRole().getName().equals("ROLE_ADMIN") && !existingUser.getEmail().equals(currentUser.getEmail())) {
            throw new UnAuthorizedException("You are not authorized to update this user");
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
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

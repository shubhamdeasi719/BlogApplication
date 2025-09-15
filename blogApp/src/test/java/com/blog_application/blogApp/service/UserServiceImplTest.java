package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Role;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.RoleNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UnAuthorizedException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.UserDto;
import com.blog_application.blogApp.repository.RoleRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ModelMapper  modelMapper;

    @InjectMocks
    UserServiceImpl  userServiceImpl;

    private User user;
    private UserDto userDto;
    private Role userRole;

    @BeforeEach
    void setUp()
    {
        userRole = new Role(1,"ROLE_USER");
        user = new User(1,"Shubham","shubham@gmail.com","shub@123","I am Java Developer",null,null,userRole);
        userDto = new UserDto(1,"shubham","shubham@gmail.com","shubh@123","I am Java Developer","ROLE_USER");

        lenient().when(modelMapper.map(any(UserDto.class), eq(User.class))).thenReturn(user);
        lenient().when(modelMapper.map(any(User.class), eq(UserDto.class))).thenReturn(userDto);
    }

    @Test
    void testRegisterUser_Success()
    {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userServiceImpl.registerUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_RoleNotFound()
    {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, ()-> userServiceImpl.registerUser(userDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_Success()
    {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(userDto.getRoleName())).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userServiceImpl.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_RoleNotFound()
    {
        when(roleRepository.findByName(userDto.getRoleName())).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, ()-> userServiceImpl.createUser(userDto));
        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success_AsAdmin()
    {
        UserDto admin = new UserDto(2,"rushikesh","rushi@gmail.com","rushi@123","I am admin","ROLE_ADMIN");
        User adminUser = new User(2,"rushikesh","rushi@gmail.com","rushi@123","I am admin",null,null,new Role(2, "ROLE_ADMIN"));

        mockSecurityContext(adminUser);

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedString");

        UserDto result = userServiceImpl.updateUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success_AsOwner()
    {
        mockSecurityContext(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedString");

        UserDto result = userServiceImpl.updateUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getEmail(),result.getEmail());

        verify(userRepository,times(1)).save(any(User.class));
    }

    @Test
    void testUpdatedUser_Unauthorized()
    {
       UserDto otherUserDto = new UserDto(3,"virat","virat@gmail.com","virat@123","I am other user","ROLE_USER");
       User otherUser = new  User(3,"virat","virat@gmail.com","virat@123","I am other user",null,null,new Role(1,"ROLE_USER"));

       mockSecurityContext(otherUser);

       when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
       when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));

       assertThrows(UnAuthorizedException.class, ()->userServiceImpl.updateUser(userDto));
       verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound()
    {
        mockSecurityContext(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()-> userServiceImpl.updateUser(userDto));
        verify(userRepository,never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success()
    {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserDto result = userServiceImpl.getUserById(1);

        assertNotNull(result);
        assertEquals(userDto.getId(),result.getId());
        assertEquals(userDto.getEmail(), result.getEmail());
    }

    @Test
    void testGetUserById_UserNotFound()
    {
        when(userRepository.findById(4)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()-> userServiceImpl.getUserById(4));
    }

    @Test
    void testGetAllUser()
    {
        User newUser = new User(5,"rohit","rohit@gmail.com","rohit@123","I am new user",null,null,new Role(1,"ROLE_USER"));
        List<User> userList = List.of(user, newUser);
        when(userRepository.findAll()).thenReturn(userList);
        lenient().when(modelMapper.map(newUser,UserDto.class)).thenReturn(new UserDto(5,"rohit","rohit@gmail.com","rohit@123","I am new user","ROLE_USER"));

        List<UserDto> allUsers = userServiceImpl.getAllUsers();

        assertNotNull(allUsers);
        assertEquals(2,allUsers.size());
        assertEquals("rohit", allUsers.get(1).getName());
    }

    @Test
    void testDeleteUser_Success()
    {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userServiceImpl.deleteUser(1);

        verify(userRepository,times(1)).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound()
    {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->userServiceImpl.deleteUser(1));

        verify(userRepository,never()).delete(any(User.class));
    }

    private void mockSecurityContext(UserDetails userDetails)
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);
    }
}

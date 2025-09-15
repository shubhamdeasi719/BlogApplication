package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Comment;
import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.entity.Role;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.CommentNotFoundException;
import com.blog_application.blogApp.exceptionHandler.PostNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UnAuthorizedException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.CommentDto;
import com.blog_application.blogApp.repository.CommentRepository;
import com.blog_application.blogApp.repository.PostRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    private User ownerUser;
    private User adminUser;
    private User otherUser;
    private Post post;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp()
    {
        ownerUser = new User();
        ownerUser.setId(1);
        ownerUser.setEmail("shubham@gmail.com");
        ownerUser.setRole(new Role(101, "ROLE_USER"));

        adminUser = new User();
        adminUser.setId(2);
        adminUser.setEmail("rushikesh@gmail.com");
        adminUser.setRole(new Role(102, "ROLE_ADMIN"));

        otherUser = new User();
        otherUser.setId(3);
        otherUser.setEmail("otheruser@gmail.com");
        otherUser.setRole(new Role(101, "ROLE_USER"));

        post = new Post();
        post.setPostId(10);
        post.setTitle("Test Post");

        comment = new Comment();
        comment.setCommentId(100);
        comment.setContent("This is a test comment");
        comment.setPost(post);
        comment.setUser(ownerUser);

        commentDto = new CommentDto();
        commentDto.setContent("This is test comment.");
    }

    @Test
    void testCreateComment_Success()
    {
        when(postRepository.findById(post.getPostId())).thenReturn(Optional.of(post));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));

        when(modelMapper.map(commentDto, Comment.class)).thenReturn(comment);

        when(commentRepository.save(comment)).thenReturn(comment);

        when(modelMapper.map(comment, CommentDto.class)).thenReturn(commentDto);

        CommentDto result = commentServiceImpl.createComment(commentDto, ownerUser.getId(), post.getPostId());

        assertNotNull(result);
        assertEquals(commentDto.getContent(), result.getContent());

        verify(postRepository, times(1)).findById(post.getPostId());
        verify(userRepository, times(1)).findById(ownerUser.getId());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testCreateComment_PostNotFound()
    {
        when(postRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> commentServiceImpl.createComment(commentDto, ownerUser.getId(), 999));

        verify(userRepository, never()).findById(anyInt());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testCreateComment_UserNotFound()
    {
        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> commentServiceImpl.createComment(commentDto, 999, post.getPostId()));

        verify(postRepository, times(1)).findById(post.getPostId());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testDeleteComment_Success_asCommentOwner()
    {
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(ownerUser);

        commentServiceImpl.deleteComment(comment.getCommentId());

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_Success_asAdmin()
    {
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(adminUser);

        commentServiceImpl.deleteComment(comment.getCommentId());

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_NotFound()
    {
        when(commentRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentServiceImpl.deleteComment(999));

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void testDeleteComment_Unauthorized()
    {
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(otherUser);

        assertThrows(UnAuthorizedException.class, () -> commentServiceImpl.deleteComment(comment.getCommentId()));

        verify(commentRepository, never()).delete(any(Comment.class));
    }
}

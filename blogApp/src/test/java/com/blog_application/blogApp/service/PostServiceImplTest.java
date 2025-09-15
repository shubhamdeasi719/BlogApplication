package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.entity.Role;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.CategoryNotFoundException;
import com.blog_application.blogApp.exceptionHandler.PostNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UnAuthorizedException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;
import com.blog_application.blogApp.repository.CategoryRepository;
import com.blog_application.blogApp.repository.PostRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    PostRepository postRepository;

    @Mock
    ModelMapper modelMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    PostServiceImpl postServiceImpl;

    private User ownerUser;
    private User admin;
    private User otherUser;
    private Category category;
    private Post post;
    private PostDto postDto;
    private List<Post> postList;
    private List<PostDto> postDtoList;

    @BeforeEach
    void setUp()
    {
        ownerUser =  new User();
        ownerUser.setId(1);
        ownerUser.setEmail("shubham@gmail.com");
        ownerUser.setRole(new Role(1,"ROLE_USER"));

        admin = new User();
        admin.setId(2);
        admin.setEmail("rushi@gmail.com");
        admin.setRole(new Role(2, "ROLE_ADMIN"));

        otherUser =  new User();
        otherUser.setId(3);
        otherUser.setEmail("virat@gmail.com");
        otherUser.setRole(new Role(3,"ROLE_USER"));


        category = new Category();
        category.setId(101);

        post = new Post();
        post.setPostId(10);
        post.setTitle("Indian Cricket Team Journey in ICC ODI WordCup");
        post.setContent("Indian cricket team is world best cricket team, Already won 2 ICC ODI world cup under Kapil dev and M.S Dhoni");
        post.setUser(ownerUser);
        post.setCategory(category);

        postDto = new PostDto();
        postDto.setId(10);
        postDto.setTitle("Updated Indian Cricket Team Journey in ICC ODI WordCup");
        postDto.setContent("Updated Indian cricket team is world best cricket team, Already won 2 ICC ODI world cup under Kapil dev and M.S Dhoni");

        // Setup for list-based tests
        Post post2 = new Post();
        post2.setPostId(11);
        post2.setTitle("Post Two Title");
        post2.setContent("Content of post two");
        post2.setUser(ownerUser);
        post2.setCategory(category);
        postList = Arrays.asList(post, post2);

        PostDto postDto2 = new PostDto();
        postDto2.setId(11);
        postDto2.setTitle("Post Two DTO Title");
        postDto2.setContent("Content of post two DTO");
        postDtoList = Arrays.asList(postDto, postDto2);
    }

    @Test
    void testCreatePost_Success()
    {
        PostDto newPostDto = new PostDto();
        newPostDto.setTitle("Indian Cricket Team Journey in ICC ODI WordCup");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(ownerUser));
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(modelMapper.map(any(PostDto.class),eq(Post.class))).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class),eq(PostDto.class))).thenReturn(newPostDto);

        PostDto createPost = postServiceImpl.createPost(newPostDto,1,101);

        assertNotNull(createPost);
        assertEquals("Indian Cricket Team Journey in ICC ODI WordCup", createPost.getTitle());

        verify(userRepository,times(1)).findById(1);
        verify(categoryRepository, times(1)).findById(101);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testCreatePost_UserNotFound()
    {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->postServiceImpl.createPost(postDto,2,101));

        verify(userRepository, times(1)).findById(2);
        verify(categoryRepository, never()).findById(anyInt());
        verify(postRepository,never()).save(any(Post.class));
    }

    @Test
    void testCreatePost_CategoryNotFound()
    {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(ownerUser));
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()-> postServiceImpl.createPost(postDto,1,999));

        verify(userRepository,times(1)).findById(1);
        verify(categoryRepository,times(1)).findById(999);
        verify(postRepository,never()).save(any(Post.class));
    }

    @Test
    void updatePost_Success_AsOwner()
    {
        mockSecurityContext(ownerUser.getEmail());
        when(modelMapper.map(any(PostDto.class), eq(Post.class))).thenReturn(post);

        when(postRepository.findById(anyInt())).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(ownerUser));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        when(modelMapper.map(any(Post.class), eq(PostDto.class))).thenReturn(postDto);

        PostDto updatedPost =postServiceImpl.updatePost(postDto);

        assertNotNull(updatedPost);
        assertEquals("Updated Indian Cricket Team Journey in ICC ODI WordCup", updatedPost.getTitle());
    }

    @Test
    void testUpdatePost_PostNotFound()
    {
        Integer nonExistingPostId = 999;
        PostDto updatedPostDto = new PostDto();
        updatedPostDto.setId(nonExistingPostId);

        Post mappedPost =  new Post();
        mappedPost.setPostId(nonExistingPostId);

        when(modelMapper.map(any(PostDto.class), eq(Post.class))).thenReturn(mappedPost);

        when(postRepository.findById(nonExistingPostId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, ()-> postServiceImpl.updatePost(updatedPostDto));

        verify(postRepository, times(1)).findById(nonExistingPostId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testUpdatePost_UnauthorizedUser()
    {
        PostDto updatedPostDto = new PostDto();
        updatedPostDto.setId(10);
        updatedPostDto.setTitle("Updated Post Title");
        updatedPostDto.setContent("Updated content of the post.");

        mockSecurityContext(otherUser.getEmail());

        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10)).thenReturn(Optional.of(post));

        when(modelMapper.map(any(PostDto.class),eq(Post.class))).thenReturn(post);

        assertThrows(UnAuthorizedException.class, ()-> postServiceImpl.updatePost(updatedPostDto));

        verify(userRepository, times(1)).findByEmail(otherUser.getEmail());
        verify(postRepository,times(1)).findById(10);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testGetPostById_Success()
    {
        when(postRepository.findById(post.getPostId())).thenReturn(Optional.of(post));
        when(modelMapper.map(any(Post.class),eq(PostDto.class))).thenReturn(postDto);

        PostDto resultDto = postServiceImpl.getPostById(post.getPostId());

        assertNotNull(resultDto);
        assertEquals(postDto.getTitle(), resultDto.getTitle());
        verify(postRepository,times(1)).findById(post.getPostId());
    }

    @Test
    void testGetPostById_PostNotFound()
    {
        when(postRepository.findById(anyInt())) .thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class,()->postServiceImpl.getPostById(89));

        verify(postRepository, times(1)).findById(89);
    }

    @Test
    void testGetAllPosts_Success()
    {
        Page<Post> page = new PageImpl<>(postList);

        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDtoList.get(0));
        when(modelMapper.map(postList.get(1), PostDto.class)).thenReturn(postDtoList.get(1));

        PostResponse result = postServiceImpl.getAllPosts(1,10,"title","asc");

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getTotalPages());
        assertEquals(2L, result.getTotalElements());
        verify(postRepository,times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetPostsByCategory_Success() {
        Page<Post> page = new PageImpl<>(postList);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(postRepository.findByCategory(any(Category.class), any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDtoList.get(0));
        when(modelMapper.map(postList.get(1), PostDto.class)).thenReturn(postDto);

        PostResponse result = postServiceImpl.getPostsByCategory(category.getId(), 0, 10, "title", "asc");

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(2L, result.getTotalElements());

        verify(categoryRepository, times(1)).findById(category.getId());
        verify(postRepository, times(1)).findByCategory(any(Category.class), any(Pageable.class));
    }

    @Test
    void testGetPostsByCategory_CategoryNotFound()
    {
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,()-> postServiceImpl.getPostsByCategory(99,0,10,"title","asc"));

        verify(categoryRepository,times(1)).findById(99);
        verify(postRepository,never()).findByCategory(any(Category.class),any(Pageable.class));
    }

    @Test
    void testGetPostByUser_Success()
    {
        Page<Post> page = new PageImpl<>(postList);

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(postRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDtoList.get(0));
        when(modelMapper.map(postList.get(1), PostDto.class)).thenReturn(postDtoList.get(1));

        PostResponse result = postServiceImpl.getPostsByUser(ownerUser.getId(),0,10,"title","asc");

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(2L, result.getTotalElements());

        verify(userRepository, times(1)).findById(ownerUser.getId());
        verify(postRepository, times(1)).findByUser(any(User.class),any(Pageable.class));
    }

    @Test
    void testGetPostsByUser_UserNotFound()
    {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()->postServiceImpl.getPostsByUser(999,0,10,"title","asc"));

        verify(userRepository,times(1)).findById(999);
        verify(postRepository,never()).findByUser(any(User.class),any(Pageable.class));
    }

    @Test
    void testSearchPosts_Success()
    {
        String keyword = "Post";

        when(postRepository.findByTitleContaining(keyword)).thenReturn(postList);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDtoList.get(0));
        when(modelMapper.map(postList.get(1), PostDto.class)).thenReturn(postDtoList.get(1));

        List<PostDto> result = postServiceImpl.searchPosts(keyword);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Post Two DTO Title",result.get(1).getTitle());
        verify(postRepository,times(1)).findByTitleContaining(keyword);
    }

    @Test
    void testDeletePost_Success()
    {
        Integer postId = 1;

        mockSecurityContext(ownerUser.getEmail());

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(ownerUser));

        postServiceImpl.deletePost(postId);

        verify(postRepository,times(1)).findById(postId);
        verify(postRepository,times(1)).delete(post);

    }

   @Test
   void testDeletePost_PostNotFound()
   {
       Integer nonExistingPostId = 999;
       when(postRepository.findById(nonExistingPostId)).thenReturn(Optional.empty());

       assertThrows(PostNotFoundException.class,()-> postServiceImpl.deletePost(nonExistingPostId));

       verify(postRepository, times(1)).findById(nonExistingPostId);
       verify(postRepository,never()).delete(any(Post.class));
   }

   @Test
   void testDeletePost_UnauthorizedUser()
   {
        mockSecurityContext(otherUser.getEmail());

        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(postRepository.findById(10)).thenReturn(Optional.of(post));

        assertThrows(UnAuthorizedException.class,()->postServiceImpl.deletePost(10));

        verify(userRepository, times(1)).findByEmail(otherUser.getEmail());
        verify(postRepository,times(1)).findById(10);
        verify(postRepository,never()).delete(any(Post.class));
   }

    private void mockSecurityContext(String username)
    {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}

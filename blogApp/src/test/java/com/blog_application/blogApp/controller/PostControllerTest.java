package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.config.AppConstants;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;
import com.blog_application.blogApp.service.FileService;
import com.blog_application.blogApp.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDto postDto;
    private PostResponse postResponse;

    @BeforeEach
    void setUp()
    {
        postDto = new PostDto();
        postDto.setId(1);
        postDto.setTitle("Indian Cricket Team");
        postDto.setContent("This is test content");
        postDto.setImageName("default.png");

        postResponse =  new PostResponse();
        postResponse.setContent(Collections.singletonList(postDto));
        postResponse.setPageNumber(0);
        postResponse.setPageSize(10);
        postResponse.setTotalElements(1L);
        postResponse.setTotalPages(1);
        postResponse.setLastPage(true);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreatePost_Success_asUser() throws Exception {
        when(postService.createPost(any(PostDto.class), anyInt(), anyInt())).thenReturn(postDto);

        mockMvc.perform(post("/api/user/{userId}/category/{categoryId}/posts",1,1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(postDto.getTitle()));

        verify(postService, times(1)).createPost(any(PostDto.class), anyInt(), anyInt());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetPostByPostId_Success_asUser() throws Exception
    {
        when(postService.getPostById(anyInt())).thenReturn(postDto);

        mockMvc.perform(get("/api/posts/{postId}", 1)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.title").value(postDto.getTitle()));
        verify(postService,times(1)).getPostById(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllPosts_Success_asAdmin() throws Exception
    {
        when(postService.getAllPosts(anyInt(),anyInt(),anyString(),anyString())).thenReturn(postResponse);

        mockMvc.perform(get("/api/posts")
                .with(csrf())
                .param("pageNumber","0")
                .param("pageSize","10"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.content[0].title").value(postDto.getTitle()));

        verify(postService, times(1)).getAllPosts(0,10, AppConstants.SORT_BY,AppConstants.SORT_DIR);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetAllPostsByCategory_Success_asUser() throws Exception{

        when(postService.getPostsByCategory(anyInt(),anyInt(),anyInt(),anyString(),anyString())).thenReturn(postResponse);

        mockMvc.perform(get("/api/category/{categoryId}/posts",1)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.content[0].title").value(postDto.getTitle()));
        verify(postService, times(1)).getPostsByCategory(1,0,5,AppConstants.SORT_BY,AppConstants.SORT_DIR);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testUpdatePost_Success_asUser() throws Exception
    {
        when(postService.updatePost(any(PostDto.class))).thenReturn(postDto);

        mockMvc.perform(put("/api/posts/update-post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(postDto.getTitle()));

        verify(postService, times(1)).updatePost(any(PostDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeletePost_Success_asAdmin() throws Exception
    {
        doNothing().when(postService).deletePost(anyInt());

        mockMvc.perform(delete("/api/posts/delete-post/{postId}", 1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully"));

        verify(postService, times(1)).deletePost(1);
    }

    @Test
    @WithMockUser(roles ={"USER"})
    void testSearchPostByTitle_Success_aasUser() throws Exception
    {
        List<PostDto> postDtos = Collections.singletonList(postDto);
        when(postService.searchPosts(anyString())).thenReturn(postDtos);

        mockMvc.perform(get("/api/posts/search/{keyword}","Test")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(postDto.getTitle()));

        verify(postService, times(1)).searchPosts("Test");
    }

    @Test
    @WithMockUser(roles ={"USER"})
    void testUploadPostImage_Success_asUser() throws Exception
    {
        MockMultipartFile file= new MockMultipartFile("image","test.jpg","/image/jpeg","some image".getBytes());
        PostDto updatedPostDto =  new PostDto();
        updatedPostDto.setImageName("test.jpg");
        when(fileService.uploadImage(anyString(), any(MockMultipartFile.class))).thenReturn("test.jpg");
        when(postService.getPostById(anyInt())).thenReturn(postDto);
        when(postService.updatePost(any(PostDto.class))).thenReturn(updatedPostDto);

        mockMvc.perform(multipart("/api/posts/image/upload/{postId}",1)
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageName").value("test.jpg"));

        verify(fileService, times(1)).uploadImage(anyString(), any(MockMultipartFile.class));
        verify(postService, times(1)).getPostById(1);
        verify(postService, times(1)).updatePost(any(PostDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testDownloadImage_Success_asUser() throws Exception
    {
        InputStream inputStream = new ByteArrayInputStream("some image data".getBytes());

        when(fileService.getResource(anyString(),anyString())).thenReturn(inputStream);

        mockMvc.perform(get("/api/post/image/{imageName}", "test.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));

        verify(fileService, times(1)).getResource(anyString(), eq("test.jpg"));
    }
}

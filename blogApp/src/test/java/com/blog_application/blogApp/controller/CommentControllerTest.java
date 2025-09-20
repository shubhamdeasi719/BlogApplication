package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.config.SecurityConfig;
import com.blog_application.blogApp.payloads.CommentDto;
import com.blog_application.blogApp.security.CustomUserDetailService;
import com.blog_application.blogApp.security.JwtTokenHelper;
import com.blog_application.blogApp.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    @MockitoBean
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    ObjectMapper objectMapper;

    private CommentDto commentDto;

    @BeforeEach
    void setUp()
    {
        commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setContent("This is test  content");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testCreateComment_Success_asUser() throws Exception {
        when(commentService.createComment(any(CommentDto.class), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post("/api/user/{userId}/post/{postId}/comments",1,1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
        .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value(commentDto.getContent()));

        verify(commentService, times(1)).createComment(any(CommentDto.class),eq(1),eq(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateComment_Success_asAdmin() throws Exception
    {
        when(commentService.createComment(any(CommentDto.class), anyInt(), anyInt())).thenReturn(commentDto);

        mockMvc.perform(post("/api/user/{userId}/post/{postId}/comments",1,1)

                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(commentDto.getContent()));
        verify(commentService, times(1)).createComment(any(CommentDto.class),eq(1),eq(1));
    }

    @Test
    void testCreateComment_Forbidden_asUnauthenticatedUser() throws Exception
    {
        mockMvc.perform(post("/api/user/{userId}/post/{postId}/comments",1,1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testDeleteComment_Success_asUser() throws Exception {
        doNothing().when(commentService).deleteComment(anyInt());

        mockMvc.perform(delete("/api/comments/{commentId}",1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));

        verify(commentService, times(1)).deleteComment(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteComment_Success_asAdmin() throws Exception
    {
        doNothing().when(commentService).deleteComment(anyInt());

        mockMvc.perform(delete("/api/comments/{commentId}",1)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));

        verify(commentService, times(1)).deleteComment(1);
    }

    @Test
    void testDeleteComment_Forbidden_asUnauthenticatedUser() throws Exception
    {
        mockMvc.perform(delete("/api/comments/{commentId}",1))
                .andExpect(status().isForbidden());
    }
}

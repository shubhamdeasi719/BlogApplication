package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;

import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto,Integer userId, Integer categoryId);

    PostDto updatePost(PostDto postDto);

    PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostDto getPostById(Integer postId);

    PostResponse getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    PostResponse getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    void deletePost(Integer postId);

    List<PostDto> searchPosts(String keyword);
}

package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.config.AppConstants;
import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;
import com.blog_application.blogApp.service.FileService;
import com.blog_application.blogApp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name="Post APIs", description = "Create - Read - Update - Delete Posts")
public class PostController {

    private PostService postService;
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    public PostController(PostService postService, FileService fileService)
    {
        this.postService = postService;
        this.fileService= fileService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/user/{userId}/category/{categoryId}/posts")
    @Operation(summary = "Create New Post, Only Admin Can Have Access")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer userId, @PathVariable Integer categoryId)
    {
        PostDto newPostDto = postService.createPost(postDto,userId,categoryId);
        return new ResponseEntity<>(newPostDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/posts/{postId}")
    @Operation(summary = "Get Single Post by Post Id, Both Admin and User Can Have Access")
    public ResponseEntity<PostDto> getPostByPostId(@PathVariable Integer postId)
    {
        PostDto postDto = postService.getPostById(postId);
        return new ResponseEntity<>(postDto,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/posts")
    @Operation(summary = "Get All Posts, also If want we can perform Paging and Sorting, Both Admin and User Can Have Access")
    public ResponseEntity<PostResponse> getAllPosts(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER,required = false)  Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value= "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir
            )
    {
        PostResponse postResponse = postService.getAllPosts(pageNumber,pageSize, sortBy, sortDir);
        return new ResponseEntity<>(postResponse,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/category/{categoryId}/posts")
    @Operation(summary = "Get All Posts of Category, also If want we can perform Paging and Sorting, Both Admin and User Can Have Access")
    public ResponseEntity <PostResponse> getAllPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER,required = false)  Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value= "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir
            )
    {
        PostResponse postResponse= postService.getPostsByCategory(categoryId,pageNumber,pageSize, sortBy, sortDir);
        return new ResponseEntity<>(postResponse,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/user/{userId}/posts")
    @Operation(summary = "Get All Posts of User, also If want we can perform Paging and Sorting, Both Admin and User Can Have Access")
    public ResponseEntity<PostResponse> getAllPostsByUser(
            @PathVariable Integer userId,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER,required = false)  Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value= "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir
            )
    {
        PostResponse postResponse = postService.getPostsByUser(userId,pageNumber,pageSize, sortBy, sortDir);
        return new ResponseEntity<>(postResponse,HttpStatus.FOUND);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping("/posts/update-post")
    @Operation(summary = "Update Post, Only Admin and Owner Can Have Access")
    public ResponseEntity<PostDto> updatePost(@Valid @RequestBody  PostDto postDto)
    {
        PostDto updatedPostDto = postService.updatePost(postDto);
        return new ResponseEntity<>(updatedPostDto,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping("/posts/delete-post/{postId}")
    @Operation(summary = "Delete Post, Only Admin and Owner Can Have Access")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId)
    {
        postService.deletePost(postId);
        ApiResponse apiResponse = new ApiResponse("Post deleted successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/posts/search/{keywords}")
    @Operation(summary = "Search Post, Both Admin and User Can Have Access")
    public ResponseEntity<List<PostDto>> searchPostByTitle(@PathVariable String keywords)
    {
        List<PostDto> postDtos = postService.searchPosts(keywords);
        return new ResponseEntity<>(postDtos,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping("/posts/image/upload/{postId}")
    @Operation(summary = "Upload Image, Both Admin and User Can Have Access")
    public ResponseEntity<PostDto> uploadPostImage(@PathVariable Integer postId, @RequestParam MultipartFile image) throws IOException {
        PostDto postDto = postService.getPostById(postId);
        String fileName = fileService.uploadImage(path, image);
        postDto.setImageName(fileName);
        PostDto updatedPost = postService.updatePost(postDto);
        return new ResponseEntity<>(updatedPost,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(value = "/post/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Get Image, Both Admin and User Can Have Access")
    public void downloadImage(@PathVariable String imageName, HttpServletResponse response) throws IOException {
        InputStream resource = fileService.getResource(path, imageName);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource,response.getOutputStream());

    }

}

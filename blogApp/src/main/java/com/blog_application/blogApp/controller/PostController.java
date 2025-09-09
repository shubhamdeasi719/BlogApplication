package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.config.AppConstants;
import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;
import com.blog_application.blogApp.service.FileService;
import com.blog_application.blogApp.service.PostService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
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

    @PostMapping("/user/{userId}/category/{categoryId}/posts")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer userId, @PathVariable Integer categoryId)
    {
        PostDto newPostDto = postService.createPost(postDto,userId,categoryId);
        return new ResponseEntity<>(newPostDto, HttpStatus.CREATED);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPostByPostId(@PathVariable Integer postId)
    {
        PostDto postDto = postService.getPostById(postId);
        return new ResponseEntity<>(postDto,HttpStatus.FOUND);
    }

    @GetMapping("/posts")
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

    @GetMapping("/category/{categoryId}/posts")
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

    @GetMapping("/user/{userId}/posts")
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


    @PutMapping("/posts/update-post")
    public ResponseEntity<PostDto> updatePost(@Valid @RequestBody  PostDto postDto)
    {
        PostDto updatedPostDto = postService.updatePost(postDto);
        return new ResponseEntity<>(updatedPostDto,HttpStatus.OK);
    }

    @DeleteMapping("/posts/delete-post/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable Integer postId)
    {
        postService.deletePost(postId);
        ApiResponse apiResponse = new ApiResponse("Post deleted successfully",true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/posts/search/{keywords}")
    public ResponseEntity<List<PostDto>> searchPostByTitle(@PathVariable String keywords)
    {
        List<PostDto> postDtos = postService.searchPosts(keywords);
        return new ResponseEntity<>(postDtos,HttpStatus.OK);
    }

    @PostMapping("/posts/image/upload/{postId}")
    public ResponseEntity<PostDto> uploadPostImage(@PathVariable Integer postId, @RequestParam MultipartFile image) throws IOException {
        PostDto postDto = postService.getPostById(postId);
        String fileName = fileService.uploadImage(path, image);
        postDto.setImageName(fileName);
        PostDto updatedPost = postService.updatePost(postDto);
        return new ResponseEntity<>(updatedPost,HttpStatus.OK);
    }

    @GetMapping(value = "/post/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public void downloadImage(@PathVariable String imageName, HttpServletResponse response) throws IOException {
        InputStream resource = fileService.getResource(path, imageName);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(resource,response.getOutputStream());

    }

}

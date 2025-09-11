package com.blog_application.blogApp.controller;

import com.blog_application.blogApp.payloads.ApiResponse;
import com.blog_application.blogApp.payloads.CommentDto;
import com.blog_application.blogApp.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name="Comment APIs", description = "Create - Delete Comments")
public class CommentController {

    private CommentService commentService;

    public CommentController(CommentService commentService)
    {
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('ADMIN) or hasRole('USER')")
    @PostMapping("/user/{userId}/post/{postId}/comments")
    @Operation(summary = "Create New Comment, Both Admin and User Can Have Access")
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto, @PathVariable Integer userId, @PathVariable Integer postId)
    {
        CommentDto newComment = commentService.createComment(commentDto, userId, postId);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN) or hasRole('USER')")
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete Comment, Both Admin and User Can Have Access")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Integer commentId)
    {
        commentService.deleteComment(commentId);
        return new ResponseEntity<>(new ApiResponse("Comment deleted successfully",true),HttpStatus.OK);
    }
}

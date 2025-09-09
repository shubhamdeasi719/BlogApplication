package com.blog_application.blogApp.service;

import com.blog_application.blogApp.payloads.CommentDto;

public interface CommentService {

    CommentDto createComment(CommentDto commentDto, Integer userId, Integer postId);

    void deleteComment(Integer commentId);

}

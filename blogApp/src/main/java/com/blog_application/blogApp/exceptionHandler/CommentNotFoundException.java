package com.blog_application.blogApp.exceptionHandler;

public class CommentNotFoundException extends RuntimeException{
    public CommentNotFoundException(String message)
    {
        super(message);
    }
}

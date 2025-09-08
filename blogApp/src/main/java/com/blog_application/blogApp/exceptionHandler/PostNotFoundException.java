package com.blog_application.blogApp.exceptionHandler;

public class PostNotFoundException extends  RuntimeException{
    public PostNotFoundException(String  message)
    {
        super(message);
    }
}

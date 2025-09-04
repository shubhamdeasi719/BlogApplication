package com.blog_application.blogApp.exceptionHandler;

public class CategoryNotFoundException extends RuntimeException{

    public CategoryNotFoundException(String message)
    {
        super(message);
    }
}

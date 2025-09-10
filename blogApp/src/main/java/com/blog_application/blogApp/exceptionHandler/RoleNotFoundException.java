package com.blog_application.blogApp.exceptionHandler;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(String message)
    {
        super(message);
    }
}

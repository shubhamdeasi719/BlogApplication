package com.blog_application.blogApp.exceptionHandler;

public class UnAuthorizedException extends RuntimeException{
    public UnAuthorizedException(String message)
    {
        super(message);
    }
}

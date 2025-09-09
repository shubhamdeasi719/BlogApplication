package com.blog_application.blogApp.payloads;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.entity.Comment;
import com.blog_application.blogApp.entity.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private Integer id;

    @NotEmpty(message = "Title cant be empty")
    private String title;

    @NotEmpty(message = "Content cant be empty")
    private String content;

    private String imageName;

    private Date addedDate;

    private UserDto user;

    private CategoryDto category;

    private List<CommentDto> comments = new ArrayList<>();
}

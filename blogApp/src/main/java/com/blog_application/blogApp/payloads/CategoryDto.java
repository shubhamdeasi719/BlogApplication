package com.blog_application.blogApp.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Integer id;

    @NotEmpty(message = "Title cant be empty")
    @Size(min = 5, message = "Title must be more than 5 chars")
    private String categoryTitle;

    @NotEmpty(message= "Description cant be empty")
    @Size(min = 10, message = "Description must be more than 10 chars")
    private String categoryDescription;
}

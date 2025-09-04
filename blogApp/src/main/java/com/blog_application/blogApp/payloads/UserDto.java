package com.blog_application.blogApp.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Integer id;

    @NotEmpty(message = "Name can't be empty!!")
    @Size(min =3, max = 10, message = "Name must be min 3 chars and max 10 chars")
    private String name;

    @NotEmpty(message = "Email can't be empty")
    @Email
    private String email;

    @NotEmpty(message = "Password can't be empty!!")
    @Size(min =3 , max = 10, message = "Password length must be min 3 and max 10")
    private String password;

    @NotEmpty(message = "Please add something in About Section !!")
    private String about;

}

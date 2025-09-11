package com.blog_application.blogApp.config;

import com.blog_application.blogApp.security.CustomUserDetailService;
import com.blog_application.blogApp.security.JwtAuthenticationFilter;
import com.blog_application.blogApp.security.JwtTokenHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig{

    private final CustomUserDetailService customUserDetailService;
    private final JwtTokenHelper jwtTokenHelper;

    public SecurityConfig(CustomUserDetailService customUserDetailService, JwtTokenHelper jwtTokenHelper)
    {
        this.customUserDetailService = customUserDetailService;
        this.jwtTokenHelper = jwtTokenHelper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Swagger endpoints: allow without authentication
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Auth endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // UserController endpoints
                        .requestMatchers("/api/users/update-user").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/create-user", "/api/users/all-users", "/api/users/one-user", "/api/users/delete-user/**").hasRole("ADMIN")

                        // CategoryController endpoints
                        .requestMatchers("/api/categories/add-category", "/api/categories/update-category", "/api/categories/delete-category/**").hasRole("ADMIN")
                        .requestMatchers("/api/categories/all-categories", "/api/categories/one-category").hasAnyRole("USER", "ADMIN")

                        // CommentController endpoints
                        .requestMatchers("/api/user/{userId}/post/{postId}/comments").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/api/comments/**").hasAnyRole("USER","ADMIN")

                        // PostController endpoints
                        .requestMatchers( "/api/user/{userId}/category/{categoryId}/posts").hasAnyRole("USER", "ADMIN")
                        .requestMatchers( "/api/posts", "/api/posts/{postId}", "/api/posts/user/{userId}", "/api/posts/category/{categoryId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers( "/api/posts/update-post").hasAnyRole("USER", "ADMIN")
                        .requestMatchers( "/api/posts/delete-post/{postId}").hasAnyRole("USER", "ADMIN")


                        // Any other request: authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenHelper,customUserDetailService),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}

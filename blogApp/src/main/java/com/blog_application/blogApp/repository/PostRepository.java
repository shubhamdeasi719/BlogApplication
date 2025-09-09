package com.blog_application.blogApp.repository;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Integer> {

     Page<Post> findByUser(User user, Pageable pageable);
     Page<Post> findByCategory(Category category, Pageable pageable);
     List<Post> findByTitleContaining(String title);

}

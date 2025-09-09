package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Comment;
import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.CommentNotFoundException;
import com.blog_application.blogApp.exceptionHandler.PostNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.CommentDto;
import com.blog_application.blogApp.repository.CommentRepository;
import com.blog_application.blogApp.repository.PostRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    private CommentRepository commentRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, ModelMapper modelMapper)
    {
        this.commentRepository  = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.modelMapper=modelMapper;
    }
    @Override
    public CommentDto createComment(CommentDto commentDto, Integer userId, Integer postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if(optionalPost.isEmpty())
        {
            throw  new PostNotFoundException("Post not found with Id: "+postId);
        }
        Post existingPost = optionalPost.get();

        User existingUser = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found with Id: "+userId));

        Comment comment = modelMapper.map(commentDto, Comment.class);
        comment.setUser(existingUser);
        comment.setPost(existingPost);

        Comment newComment = commentRepository.save(comment);
        return modelMapper.map(newComment,CommentDto.class);
    }

    @Override
    public void deleteComment(Integer commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if(optionalComment.isEmpty())
        {
            throw new CommentNotFoundException("Comment not found with Id: "+commentId);
        }

        Comment comment = optionalComment.get();
        commentRepository.delete(comment);
    }
}


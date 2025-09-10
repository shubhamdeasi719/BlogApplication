package com.blog_application.blogApp.service;

import com.blog_application.blogApp.entity.Category;
import com.blog_application.blogApp.entity.Post;
import com.blog_application.blogApp.entity.User;
import com.blog_application.blogApp.exceptionHandler.CategoryNotFoundException;
import com.blog_application.blogApp.exceptionHandler.PostNotFoundException;
import com.blog_application.blogApp.exceptionHandler.UnAuthorizedException;
import com.blog_application.blogApp.exceptionHandler.UserNotFoundException;
import com.blog_application.blogApp.payloads.PostDto;
import com.blog_application.blogApp.payloads.PostResponse;
import com.blog_application.blogApp.repository.CategoryRepository;
import com.blog_application.blogApp.repository.PostRepository;
import com.blog_application.blogApp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService{

    private PostRepository postRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository, ModelMapper modelMapper)
    {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public PostDto createPost(PostDto postDto,Integer userId, Integer categoryId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+userId);
        }

        User user = optionalUser.get();

        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if(optionalCategory.isEmpty())
        {
            throw new CategoryNotFoundException("Category not found with id: "+categoryId);
        }

        Category category = optionalCategory.get();

        Post post = dtoToEntity(postDto);

        if(post.getImageName() == null || post.getImageName().isEmpty())
        {
            post.setImageName("default-image.jpg");
        }
        post.setAddedDate(new Date());
        post.setUser(user);
        post.setCategory(category);

        Post newPost = postRepository.save(post);
        return entityToDto(newPost);
    }

    @Override
    public PostDto updatePost(PostDto postDto) {
        Post post =  dtoToEntity(postDto);
        Optional<Post> optionalPost = postRepository.findById(post.getPostId());
        if(optionalPost.isEmpty())
        {
            throw new PostNotFoundException("Post not found with id: "+post.getPostId());
        }

        Post existingPost = optionalPost.get();

        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + currentUsername));

        if (!currentUser.getRole().getName().equals("ROLE_ADMIN") &&
                !existingPost.getUser().getId().equals(currentUser.getId())) {
            throw new UnAuthorizedException("You are not authorized to update this post");
        }

        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        if (post.getImageName() == null || post.getImageName().isEmpty())
        {
            existingPost.setImageName("default-image.jpg");
        }else{
            existingPost.setImageName(post.getImageName());
        }

        Post updatedPost = postRepository.save(existingPost);

        return entityToDto(updatedPost);
    }

    @Override
    public PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable page = PageRequest.of(pageNumber,pageSize, sort);
        Page<Post> pagePost = postRepository.findAll(page);

        List<Post> posts = pagePost.getContent();
        List<PostDto>postDtos = posts.stream().map((post) -> entityToDto(post)).toList();

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNumber(pagePost.getNumber());
        postResponse.setPageSize(pagePost.getSize());
        postResponse.setTotalElements(pagePost.getTotalElements());
        postResponse.setTotalPages(pagePost.getTotalPages());
        postResponse.setLastPage(pagePost.isLast());

        return postResponse;
    }

    @Override
    public PostDto getPostById(Integer postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if(optionalPost.isEmpty())
        {
            throw new PostNotFoundException("Post not available with id: "+postId);
        }

        Post post = optionalPost.get();
        return entityToDto(post);
    }

    @Override
    public PostResponse getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if(optionalCategory.isEmpty())
        {
            throw new CategoryNotFoundException("Category not  found with id: "+categoryId);
        }

        Category existingCategory = optionalCategory.get();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable page = PageRequest.of(pageNumber,pageSize,sort);
        Page<Post> pagePosts = postRepository.findByCategory(existingCategory, page);
        List<Post> posts = pagePosts.getContent();
        List<PostDto> postDtos = posts.stream().map(post -> entityToDto(post)).toList();

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNumber(pagePosts.getNumber());
        postResponse.setPageSize(pagePosts.getSize());
        postResponse.setTotalElements(pagePosts.getTotalElements());
        postResponse.setTotalPages(pagePosts.getTotalPages());
        postResponse.setLastPage(pagePosts.isLast());

        return postResponse;
    }

    @Override
    public PostResponse getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty())
        {
            throw new UserNotFoundException("User not found with id: "+userId);
        }

        User user = optionalUser.get();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable page = PageRequest.of(pageNumber,pageSize,sort);
        Page<Post> pagePosts = postRepository.findByUser(user,page);
        List<Post> userPosts =pagePosts.getContent();
        List<PostDto> postDtos = userPosts.stream().map(post -> entityToDto(post)).toList();

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNumber(pagePosts.getNumber());
        postResponse.setPageSize(pagePosts.getSize());
        postResponse.setTotalElements(pagePosts.getTotalElements());
        postResponse.setTotalPages(pagePosts.getTotalPages());
        postResponse.setLastPage(pagePosts.isLast());

        return postResponse;
    }

    @Override
    public void deletePost(Integer postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if(optionalPost.isEmpty())
        {
            throw new PostNotFoundException("Post not found with id: "+postId);
        }

        Post post = optionalPost.get();

        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + currentUsername));

        if (!currentUser.getRole().getName().equals("ROLE_ADMIN") &&
                !post.getUser().getId().equals(currentUser.getId())) {
            throw new UnAuthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Override
    public List<PostDto> searchPosts(String keyword)
    {
        List<Post> posts = postRepository.findByTitleContaining(keyword);
        List<PostDto> postDtos = posts.stream().map(post -> entityToDto(post)).toList();
        return postDtos;
    }

    public Post dtoToEntity(PostDto postDto)
    {
        Post post = modelMapper.map(postDto,Post.class);
        return post;
    }

    public PostDto entityToDto(Post post)
    {
        PostDto postDto = modelMapper.map(post,PostDto.class);
        return postDto;
    }
}

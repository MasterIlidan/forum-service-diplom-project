package ru.students.forumservicediplomproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void savePost(Post post) {
        postRepository.save(post);
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public void deletePost(Post post) {
        postRepository.delete(post);
    }

    @Override
    public void updatePost(Post post) {
        postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public List<Post> getAllPostsByThread(Thread thread) {
        return postRepository.findByThread(thread);
    }
    @Override
    public List<Object[]> countPostsByThread(Thread threadId) {
        return postRepository.countTotalPostsByThread(threadId);
    }
}

package ru.students.forumservicediplomproject.service;

import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.Optional;

public interface PostService {


    long savePost(MultipartFile torrentFile, PostDto postDto, long threadId, long forumId);

    Optional<Post> getPostById(long id);
    void deletePost(Post post);
    void updatePost(Post post);
    List<Post> getAllPosts();

    List<Post> getAllPostsByThread(Thread thread);

    List<Object[]> countPostsByThread(Thread threadId);
}

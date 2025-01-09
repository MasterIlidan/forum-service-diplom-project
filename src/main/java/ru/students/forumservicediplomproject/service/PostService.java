package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.Optional;

public interface PostService {
    void savePost(Post Post);
    Optional<Post> getPostById(long id);
    void deletePost(Post post);
    void updatePost(Post post);
    List<Post> getAllPosts();

    List<Post> getAllPostsByThread(Thread thread);
}

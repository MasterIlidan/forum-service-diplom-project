package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface PostService {


    long savePost(MultipartFile torrentFile, PostDto postDto, Thread threadId, long forumId);

    void changePost(Post post);

    void changePostStatus(String hash, String status);

    Optional<Post> getPostById(long id);

    Post getPostByHashInfo(String hash);

    void deleteAllByThread(Thread thread);

    void deletePost(Post post);
    void updatePost(Post post);
    List<Post> getAllPosts();

    HashMap<Post, Peers> getPeers(List<Post> postList);

    @Nullable
    Post getLastCreatedPost();

    List<Post> getAllPostsByThread(Thread thread);

    List<Object[]> countPostsByThread(Thread threadId);

    long getCountOfAllPosts();

    void registerNewTorrent(MultipartFile torrentFile, Post post);

    void approvePost(long postId);

    List<Post> getPostsWithNewStatus();
}

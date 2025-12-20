package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;

import java.util.HashMap;
import java.util.List;

public interface PeersService {
    void updatePeers();
    Peers getPeersForPost(Post post);

    List<Peers> getPeersForPosts(List<Post> postsList);

    HashMap<String, Long> getCountOfAllPeers();
}

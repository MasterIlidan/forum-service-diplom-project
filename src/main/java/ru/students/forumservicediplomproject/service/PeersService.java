package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;

import java.util.HashMap;

public interface PeersService {
    void updatePeers();
    Peers getPeers(Post post);

    HashMap<String, Long> getCountOfAllPeers();
}

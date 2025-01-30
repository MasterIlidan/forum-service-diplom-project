package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;

public interface PeersService {
    void updatePeers();
    Peers getPeers(Post post);
}

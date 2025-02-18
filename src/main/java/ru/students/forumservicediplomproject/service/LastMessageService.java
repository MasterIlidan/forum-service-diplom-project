package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;

import java.util.HashMap;
import java.util.List;

public interface LastMessageService {
    void saveLastMessage(Message message);

    LastMessage getLastMessageByPost(Forum forum);

    HashMap<Forum, LastMessage> getAllLastMessagesByForums(List<Forum> forumList);

    LastMessage getLastMessageByThread(Thread thread);

    HashMap<Thread, LastMessage> getAllLastMessagesByThreads(List<Thread> threadList);

    LastMessage getLastMessageByPost(Post post);

    HashMap<Post, LastMessage> getAllLastMessagesByPosts(List<Post> postList);

    void deleteByThread(Thread thread);

    void deleteByPost(Post post);

    void deleteByForum(Forum forum);
}

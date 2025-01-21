package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;

public interface SearchService {
    List<Thread> threadResult(String keyWord);
    List<Post> postResult(String keyWord);
    List<Message> messageResult(String keyWord);
}

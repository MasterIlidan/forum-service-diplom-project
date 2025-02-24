package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;

public interface ThreadService {


    void saveThread(ThreadDto threadDto, Forum forumId);

    Thread getThreadById(long id);

    void deleteAllThreadsByForum(Forum forum);

    void deleteThread(Thread thread);
    void updateThread(Thread thread);
    List<Thread> getAllThreads();
    List<Thread> getAllThreadsByForum(Forum forum);

    Long countTotalThreadsByForum(Forum forumId);
}

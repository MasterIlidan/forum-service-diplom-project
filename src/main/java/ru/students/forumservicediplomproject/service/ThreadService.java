package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.Optional;

public interface ThreadService {


    void saveThread(ThreadDto threadDto, long forumId);

    Optional<Thread> getThreadById(long id);
    void deleteThread(Thread thread);
    void updateThread(Thread thread);
    List<Thread> getAllThreads();
    List<Thread> getAllThreadsByForum(Long forumId);

    List<Object[]> countTotalThreadsByForum(Forum forumId);
}

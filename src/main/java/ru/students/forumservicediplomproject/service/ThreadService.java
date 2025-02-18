package ru.students.forumservicediplomproject.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.Optional;

public interface ThreadService {


    void saveThread(ThreadDto threadDto, Forum forumId);

    Optional<Thread> getThreadById(long id);

    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    void deleteAllThreadsByForum(Forum forum);

    void deleteThread(Thread thread);
    void updateThread(Thread thread);
    List<Thread> getAllThreads();
    List<Thread> getAllThreadsByForum(Forum forum);

    List<Object[]> countTotalThreadsByForum(Forum forumId);
}

package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.repository.ThreadRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ThreadServiceImpl implements ThreadService {

    private final ThreadRepository threadRepository;
    private final UserService userService;
    private final PostService postService;

    public ThreadServiceImpl(ThreadRepository threadRepository, UserService userService, PostService postService) {
        this.threadRepository = threadRepository;
        this.userService = userService;
        this.postService = postService;
    }

    @Override
    public void saveThread(ThreadDto threadDto, Forum forum) {
        Thread thread = new Thread();
        thread.setThreadName(threadDto.getThreadName());
        thread.setCreatedBy(userService.getCurrentUserCredentials());
        thread.setCreationDate(new Timestamp(new Date().getTime()));
        thread.setForumId(forum);

        threadRepository.save(thread);
    }

    @Override
    public Thread getThreadById(long id) {
        Optional<Thread> optionalThread = threadRepository.findByThreadId(id);
        if (optionalThread.isEmpty()) {
            throw  new ResourceNotFoundException("Ветка не найдена! Id %d".formatted(id));
        }
        return optionalThread.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteAllThreadsByForum(Forum forum) {
        List<Thread> threads = getAllThreadsByForum(forum);
        threads.forEach(this::deleteThread);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteThread(Thread thread) {
        postService.deleteAllByThread(thread);

        threadRepository.delete(thread);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void updateThread(Thread thread) {
        threadRepository.save(thread);
    }

    @Override
    public List<Thread> getAllThreads() {
        return threadRepository.findAll();
    }

    @Override
    public List<Thread> getAllThreadsByForum(Forum forum) {
        return threadRepository.findByForumId(forum);
    }

    @Override
    public Long countTotalThreadsByForum(Forum forumId) {
        return threadRepository.countAllByForumId(forumId);
    }

    @Override
    public Long countTotalPostsInThreadsByForum(Forum forumId) {
        long total = 0;
        List<Thread> threadList = getAllThreadsByForum(forumId);
        for (Thread thread: threadList) {
            total += postService.countPostsByThread(thread);
        }
        return total;
    }

    @Override
    public Long countTotalMessagesInThreadsByForum(Forum forumId) {
        long total = 0;
        List<Thread> threadList = getAllThreadsByForum(forumId);
        for (Thread thread: threadList) {
            total += postService.countTotalMessagesInPostsByThread(thread);
        }
        return total;
    }
}

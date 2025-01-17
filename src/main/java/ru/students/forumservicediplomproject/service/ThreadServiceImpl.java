package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.repository.ThreadRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ThreadServiceImpl implements ThreadService {

    private final ThreadRepository threadRepository;
    private final ForumServiceImpl forumServiceImpl;

    public ThreadServiceImpl(ThreadRepository threadRepository, ForumServiceImpl forumServiceImpl) {
        this.threadRepository = threadRepository;
        this.forumServiceImpl = forumServiceImpl;
    }

    @Override
    public void saveThread(Thread thread) {
        threadRepository.save(thread);
    }

    @Override
    public Optional<Thread> getThreadById(long id) {
        return threadRepository.findById(id);
    }

    @Override
    public void deleteThread(Thread thread) {
        threadRepository.delete(thread);
    }

    @Override
    public void updateThread(Thread thread) {
        threadRepository.save(thread);
    }

    @Override
    public List<Thread> getAllThreads() {
        return threadRepository.findAll();
    }

    @Override
    public List<Thread> getAllThreadsByForum(Long forumId) {
        Optional<Forum> forum = forumServiceImpl.getForum(forumId);
        if (forum.isPresent()) {
            return threadRepository.findByForumId(forum.get());
        }
        return List.of();
    }

    @Override
    public List<Object[]> countTotalThreadsByForum(Forum forumId) {
        return threadRepository.countTotalThreadsByForumId(forumId);
    }
}

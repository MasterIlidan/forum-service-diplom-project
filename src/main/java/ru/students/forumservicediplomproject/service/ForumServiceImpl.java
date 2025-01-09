package ru.students.forumservicediplomproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.repository.ForumRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private ForumRepository forumRepository;

    @Override
    public void saveForum(Forum forum) {
        forumRepository.save(forum);
    }

    @Override
    public Optional<Forum> getForum(long id) {
        return forumRepository.findById(id);
    }

    @Override
    public List<Forum> getAllForums() {
        return forumRepository.findAll();
    }

    @Override
    public void deleteForum(long id) {
        forumRepository.deleteById(id);
    }

    @Override
    public void updateForum(Forum forum) {
        forumRepository.save(forum);
    }

    @Override
    public List<Object[]> countTotalThreads(Long forumId) {
        Optional<Forum> forum = getForum(forumId);
        if (forum.isPresent()) {
            return forumRepository.countTotalThreadsByThreadsName(forum.get());
        } else {
            throw new RuntimeException();
        }


    }
}

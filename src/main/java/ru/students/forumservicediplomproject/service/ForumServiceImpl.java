package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.repository.ForumRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ForumServiceImpl implements ForumService {

    private final ForumRepository forumRepository;
    private final UserService userService;
    private final ThreadService threadService;

    public ForumServiceImpl(ForumRepository forumRepository, UserService userService, ThreadService threadService) {
        this.forumRepository = forumRepository;
        this.userService = userService;
        this.threadService = threadService;
    }

    @Override
    public void saveForum(ForumDto forumDto) {
        Forum forum = new Forum();

        forum.setForumName(forumDto.getForumName());
        forum.setDescription(forumDto.getDescription());
        forum.setCreatedBy(userService.getCurrentUserCredentials());
        forum.setCreationDate(new Timestamp(new Date().getTime()));
        forumRepository.save(forum);
    }

    @Override
    public Optional<Forum> getForum(long id) {
        return forumRepository.findByForumId(id);
    }

    @Override
    public List<Forum> getAllForums() {
        return forumRepository.findAll();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteForum(long id) {
        Optional<Forum> optionalForum = getForum(id);
        if (optionalForum.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        threadService.deleteAllThreadsByForum(optionalForum.get());
        forumRepository.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void updateForum(Forum forum) {
        forumRepository.save(forum);
    }

    @Override
    public Long countTotalForums(Long forumId) {
        return forumRepository.count();
    }
}

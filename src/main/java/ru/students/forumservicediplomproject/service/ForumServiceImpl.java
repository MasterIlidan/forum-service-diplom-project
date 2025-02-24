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
    private final MessageService messageService;

    public ForumServiceImpl(ForumRepository forumRepository, UserService userService, ThreadService threadService, MessageService messageService) {
        this.forumRepository = forumRepository;
        this.userService = userService;
        this.threadService = threadService;
        this.messageService = messageService;
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
    public Forum getForum(long forumId) {
        Optional<Forum> optionalForum = forumRepository.findByForumId(forumId);
        if (optionalForum.isEmpty()) {
            throw new ResourceNotFoundException("Форум не найден! Id %d".formatted(forumId));
        }
        Forum forum = optionalForum.get();
        forum.setTotalThreadsInForum(threadService.countTotalThreadsByForum(forum));
        forum.setTotalPostsInForum(threadService.countTotalPostsInThreadsByForum(forum));
        forum.setTotalMessagesInForum(threadService.countTotalMessagesInThreadsByForum(forum));
        forum.setLastMessageInForum(messageService.getLastMessageByForum(forum));

        return forum;
    }

    @Override
    public List<Forum> getAllForums() {
        List<Forum> forumList = forumRepository.findAll();
        for (Forum forum:forumList) {
            forum.setTotalThreadsInForum(threadService.countTotalThreadsByForum(forum));
            forum.setTotalPostsInForum(threadService.countTotalPostsInThreadsByForum(forum));
            forum.setTotalMessagesInForum(threadService.countTotalMessagesInThreadsByForum(forum));
            forum.setLastMessageInForum(messageService.getLastMessageByForum(forum));
        }
        return forumList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteForum(long id) {
        Forum forum = getForum(id);

        threadService.deleteAllThreadsByForum(forum);
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

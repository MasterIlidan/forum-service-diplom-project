package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.repository.ForumRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ForumServiceImpl implements ForumService {

    private final ForumRepository forumRepository;
    private final UserService userService;

    public ForumServiceImpl(ForumRepository forumRepository, UserService userService) {
        this.forumRepository = forumRepository;
        this.userService = userService;
    }

    @Override
    public void saveForum(ForumDto forumDto) {
        Forum forum = new Forum();

        forum.setForumName(forumDto.getForumName());
        forum.setDescription(forumDto.getDescription());
        forum.setCreatedBy(userService.getCurrentUserCredentials());
        forum.setCreationDate(Date.valueOf(LocalDate.now()));
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
    public List<Object[]> countTotalForums(Long forumId) {
        return forumRepository.countTotalForums();
    }
}

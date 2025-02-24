package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;

public interface ForumService {

    void saveForum(ForumDto forumDto);

    Forum getForum(long id);

    List<Forum> getAllForums();

    void deleteForum(long id);

    void updateForum(Forum forum);

    Long countTotalForums(Long forumId);
}

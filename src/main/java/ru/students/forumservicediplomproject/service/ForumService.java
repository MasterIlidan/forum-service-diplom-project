package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;
import java.util.Optional;

interface ForumService {
    void saveForum(Forum forum);

    Optional<Forum> getForum(long id);

    List<Forum> getAllForums();

    void deleteForum(long id);

    void updateForum(Forum forum);

}

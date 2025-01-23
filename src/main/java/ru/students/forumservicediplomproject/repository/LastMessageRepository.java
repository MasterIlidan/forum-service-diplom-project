package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.LastMessage;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

@Repository
public interface LastMessageRepository extends JpaRepository<LastMessage, Forum> {

    LastMessage findByForum(Forum forum);

    LastMessage findByThread(Thread thread);

    LastMessage findByPost(Post post);

    void deleteByPost(Post post);

    void deleteByThread(Thread thread);

    void deleteByForum(Forum forum);
}

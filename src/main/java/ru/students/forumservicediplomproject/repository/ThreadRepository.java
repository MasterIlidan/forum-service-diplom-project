package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    List<Thread> findByForumId(Forum forumId);

    @Query("SELECT t.forumId, COUNT(t.forumId) FROM Thread AS t where t.forumId=:#{#forumId}")
    List<Object[]> countTotalThreadsByForumId(Forum forumId);

    Optional<Thread> findByThreadId(long threadId);
}

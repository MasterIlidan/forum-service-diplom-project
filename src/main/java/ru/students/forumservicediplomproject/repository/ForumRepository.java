package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {

    @Query("SELECT t.forumId, COUNT(t.forumId) FROM Forum AS t")
    List<Object[]> countTotalForums();

    Optional<Forum> findByForumId(long forumId);
}

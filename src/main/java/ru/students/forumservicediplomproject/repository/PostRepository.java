package ru.students.forumservicediplomproject.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByThread(@NotNull Thread thread);

    @Query("SELECT t.thread, COUNT(t.thread) FROM Post AS t where t.thread=:#{#thread}")
    List<Object[]> countTotalPostsByThread(@NotNull Thread thread);

    Post findByHashInfo(@NotNull String hashInfo);

    List<Post> findAllByCreationDateBeforeOrderByCreationDateDesc(@NotNull Timestamp creationDateBefore);
}

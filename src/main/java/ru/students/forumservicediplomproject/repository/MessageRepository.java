package ru.students.forumservicediplomproject.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByPostId(@NotNull Post postId);
    @Query("SELECT t.postId, COUNT(t.postId) FROM Message AS t where t.postId=:#{#postId}")
    List<Object[]> countTotalMessagesByPostId(@NotNull Post postId);

    @Query("SELECT p FROM Message p WHERE p.messageId = :postId ORDER BY p.creationDate DESC LIMIT 1")
    Message findLatestPostByPost(@Param("postId") Long postId);

    @Query("SELECT p FROM Message p WHERE p.postId.thread.threadId = :threadId ORDER BY p.creationDate DESC LIMIT 1")
    Message findLatestPostByThread(@Param("threadId") Long threadId);

    @Query("SELECT p FROM Message p WHERE p.postId.thread.forumId.forumId = :forumId ORDER BY p.creationDate DESC LIMIT 1")
    Message findLatestPostByForum(@Param("forumId") Long forumId);
}

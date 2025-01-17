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
}

package ru.students.forumservicediplomproject.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findByThread(@NotNull Thread thread);

    @Query("SELECT t.postId, COUNT(t.postId) FROM Post AS t where t.postId=:#{#postId}")
    List<Object[]> countTotalMessagesByPostId(@Param("postId") Post post);
}

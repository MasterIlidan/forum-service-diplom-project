package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

}

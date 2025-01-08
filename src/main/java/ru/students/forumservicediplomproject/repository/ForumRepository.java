package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {
}

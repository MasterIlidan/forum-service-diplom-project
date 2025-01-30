package ru.students.forumservicediplomproject.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;

public interface PeersRepository extends JpaRepository<Peers, Post> {
    Peers findByPost(@NotNull Post post);
}

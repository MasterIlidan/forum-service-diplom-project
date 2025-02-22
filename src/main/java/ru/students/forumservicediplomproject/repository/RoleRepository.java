package ru.students.forumservicediplomproject.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.students.forumservicediplomproject.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String name);

    boolean existsByRoleName(@NotNull String roleName);
}

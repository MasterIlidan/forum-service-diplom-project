package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.students.forumservicediplomproject.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String name);
}

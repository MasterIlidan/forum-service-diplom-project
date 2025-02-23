package ru.students.forumservicediplomproject.service;


import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void saveNewUser(UserDto userDto);

    Role getUserRole();

    Role getRoleByEnum(UserServiceImpl.UserRoles role);

    Role getRoleByName(String roleName);

    void updateUser(UserDto userDto);

    User findUserByEmail(String email);

    Optional<User> findUserById(Long id);

    List<UserDto> findAllUsers();

    UserDto mapToUserDto(User user);

    User getCurrentUserCredentials();

    boolean isUserPrivileged(User currentUserCredentials);

    boolean isUserAdmin(User currentUserCredentials);
}

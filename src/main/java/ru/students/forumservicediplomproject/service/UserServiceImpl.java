package ru.students.forumservicediplomproject.service;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.repository.RoleRepository;
import ru.students.forumservicediplomproject.repository.UserRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

        checkRolesExist();
        checkAdminUserExist();
    }

    private void checkAdminUserExist() {
        if (!userRepository.existsByRoles(List.of(getRoleByEnum(UserRoles.ADMIN)))) {
            User user = new User();
            user.setUserName("Admin");
            user.setEmail("admin@admin.com");
            user.setPassword(passwordEncoder.encode("$$99adminOfThisThing99$$"));
            user.setRegistrationDate(new Timestamp(new Date().getTime()));
            userRepository.save(user);
        }
    }

    private void checkRolesExist() {
        for (var roleEnum : UserRoles.values()) {
            if (!roleRepository.existsByRoleName(roleEnum.name())) {
                Role role = new Role();
                role.setRoleName(roleEnum.name());
                roleRepository.save(role);
            }
        }
    }

    @Override
    public void saveNewUser(UserDto userDto) {
        User user = new User();
        user.setUserName(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRegistrationDate(new Timestamp(new Date().getTime()));

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles(Collections.singletonList(getUserRole()));

        userRepository.save(user);
    }

    private Role getUserRole() {
        return roleRepository.findByRoleName(UserRoles.USER.name());
    }
    private Role getRoleByEnum(UserRoles role) {
        return roleRepository.findByRoleName(role.name());
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setId(user.getUserId());
        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getRoleName());
        }
        userDto.setRoles(roles);
        return userDto;
    }

    public User getCurrentUserCredentials() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        return findUserByEmail(currentUser.getUsername());
    }

    @Override
    public boolean isUserPrivileged(User currentUserCredentials) {
        for (Role role:currentUserCredentials.getRoles()) {
            if (role.getRoleName().equals(UserRoles.ADMIN.name()) ||
                    role.getRoleName().equals(UserRoles.MODERATOR.name())) {
                return true;
            }
        }
        return false;
    }

    public enum UserRoles {
        USER, MODERATOR, ADMIN
    }
}

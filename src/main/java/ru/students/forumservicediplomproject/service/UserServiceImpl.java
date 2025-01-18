package ru.students.forumservicediplomproject.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.repository.RoleRepository;
import ru.students.forumservicediplomproject.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setUserName(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRegistrationDate(new Timestamp(new Date().getTime()));

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Role role = roleRepository.findByRoleName("READ_ONLY");
        if (role == null) {
            role = checkAdminRoleExist();
        }
        user.setRoles(List.of(role));
        userRepository.save(user);
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
        for (Role role:user.getRoles()) {
            roles.add(role.getRoleName());
        }
        userDto.setRoles(roles);
        return userDto;
    }
    private Role checkAdminRoleExist() {
        Role role = new Role();
        role.setRoleName("ADMIN");
        return roleRepository.save(role);
    }
    public User getCurrentUserCredentials() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        return findUserByEmail(currentUser.getUsername());
    }
}

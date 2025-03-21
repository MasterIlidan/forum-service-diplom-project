package ru.students.forumservicediplomproject.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Resource;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.repository.RoleRepository;
import ru.students.forumservicediplomproject.repository.UserRepository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceService resourceService;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, ResourceService resourceService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

        checkRolesExist();
        checkAdminUserExist();
        this.resourceService = resourceService;
    }

    private void checkAdminUserExist() {
        if (!userRepository.existsByRoles(List.of(getRoleByEnum(UserRoles.ADMIN)))) {
            User user = new User();
            user.setUserName("Admin");
            user.setEmail("admin@admin.com");
            user.setPassword(passwordEncoder.encode("$$99adminOfThisThing99$$"));
            user.setRegistrationDate(new Timestamp(new Date().getTime()));
            user.setRoles(List.of(roleRepository.findByRoleName("ADMIN")));
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
    @Override
    public Role getUserRole() {
        return roleRepository.findByRoleName(UserRoles.USER.name());
    }
    @Override
    public Role getRoleByEnum(UserRoles role) {
        return roleRepository.findByRoleName(role.name());
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public Resource loadUserAvatar(User user) {
        if (user.getAvatar() != null) {
            resourceService.getResource(user.getAvatar());
        }
        return user.getAvatar();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void updateUser(UserDto userDto) {
        Optional<User> optionalUser = findUserById(userDto.getId());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        User user = optionalUser.get();
        User currentUserCredentials = getCurrentUserCredentials();

        user.setUserName(userDto.getUsername());
        if (isUserAdmin(currentUserCredentials)) {
            user.setEmail(user.getEmail());

            List<Role> roles = new ArrayList<>(userDto.getRoles().stream().map(this::getRoleByName).toList());
            user.setRoles(roles);
        }

        if (!userDto.getAvatar().isEmpty()) {
            Resource resource = resourceService.registerNewResource(userDto.getAvatar());
            if (resource != null) {
                //resourceService.saveResource(resource);
                user.setAvatar(resource);
            } else {
                log.error("При сохранении аватара произошла ошибка");
            }
        }

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

    @Override
    public UserDto mapToUserDto(User user) {
        Resource avatar = loadUserAvatar(user);
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setId(user.getUserId());
        userDto.setRegistrationDate(user.getRegistrationDate());
        if (user.getAvatar() != null) {
            userDto.setBase64Avatar(avatar.getBase64Image());
        }
        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getRoleName());
        }
        userDto.setRoles(roles);
        return userDto;
    }
    @Override
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

    @Override
    public boolean isUserAdmin(User currentUserCredentials) {
        for (Role role:currentUserCredentials.getRoles()) {
            if (role.getRoleName().equals(UserRoles.ADMIN.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден! Id %d".formatted(userId));
        }
        loadUserAvatar(optionalUser.get());
        return optionalUser.get();
    }

    public enum UserRoles {
        USER, MODERATOR, ADMIN
    }
}

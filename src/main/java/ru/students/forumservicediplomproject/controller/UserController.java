package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.controller.form.Search;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.service.UserService;
import ru.students.forumservicediplomproject.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users-list");
        List<UserDto> users = userService.findAllUsers();

        UserServiceImpl.UserRoles[] roleNames = UserServiceImpl.UserRoles.values();

        modelAndView.addObject("roleNames", roleNames);
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @GetMapping("/user/{userId}")
    public ModelAndView userProfile(@PathVariable Long userId) {
        ModelAndView modelAndView = new ModelAndView("profile-page");
        User currentUserCredentials = userService.getCurrentUserCredentials();
        User userById = userService.getUserById(userId);

        UserDto user = userService.mapToUserDto(userById);

        boolean isThisUserPage = currentUserCredentials.getUserId() == user.getId();

        modelAndView.addObject("search", new Search());
        modelAndView.addObject("isEditAllowed", isThisUserPage);

        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/user")
    public String userProfile() {
        User user = userService.getCurrentUserCredentials();

        return "redirect:/user/%d".formatted(user.getUserId());
    }


    @GetMapping("/register")
    public String registrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "/register";
    }

    @PostMapping("/register/save")
    public String saveUserAfterRegistration(@Valid @ModelAttribute("user") UserDto userDto,
                                            BindingResult result,
                                            Model model) {
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null,
                    "User with this email already exist");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/register";
        }
        userService.saveNewUser(userDto);
        return "redirect:/register?success";
    }

    @GetMapping("/user/{userId}/editForm")
    public ModelAndView updateUserForm(@PathVariable Long userId) {
        User currentUser = userService.getCurrentUserCredentials();
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с id {} не найден!", userId);
            throw new ResourceNotFoundException("Пользователь с id %d не найден!".formatted(userId));
        }

        if (!userService.isUserAdmin(currentUser) & currentUser.getUserId() != optionalUser.get().getUserId()) {
            log.warn("Пользователь не является администратором, чтобы редактировать данные другого пользователя");
            throw new AccessDeniedException("Недостаточно прав для изменения данных");
        }

        if (!userService.isUserAdmin(currentUser) & currentUser.getRoles() != optionalUser.get().getRoles()) {
            log.warn("Пользователь не является администратором, чтобы редактировать роли другого пользователя");
            throw new AccessDeniedException("Недостаточно прав для изменения роли");
        }


        return getModelAndView(optionalUser.get());
    }

    private ModelAndView getModelAndView(User user) {

        ModelAndView mav = new ModelAndView("forms/user-edit-form");

        UserDto userUpdate = new UserDto();

        userUpdate.setId(user.getUserId());
        userUpdate.setUsername(user.getUserName());
        userUpdate.setEmail(user.getEmail());

        List<String> roles = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roles.add(role.getRoleName());
        }

        userUpdate.setRoles(roles);
        List<String> roleNames = Arrays.stream(UserServiceImpl.UserRoles.values()).map(Enum::name).toList();

        mav.addObject("roleNames", roleNames);
        mav.addObject("user", userUpdate);
        return mav;
    }

    @PostMapping("/user/{userId}")
    public String updateUser(@PathVariable long userId, UserDto userDto) {
        try {
            userService.updateUser(userDto);
        } catch (ResourceNotFoundException e) {
            log.error("Пользователь с id {} не найден!", userId);
            throw new ResourceNotFoundException("Пользователь с id %d не найден!".formatted(userId));
        }
        return "redirect:/users";
    }

}

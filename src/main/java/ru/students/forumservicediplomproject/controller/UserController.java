package ru.students.forumservicediplomproject.controller;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.service.UserService;

import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user{userId}")
    public ModelAndView userProfile(@PathVariable @Nullable Long userId, Model model) {
        ModelAndView modelAndView = new ModelAndView("profile-page");
        Optional<User> user;
        if (userId == null) {
            user = Optional.of(userService.getCurrentUserCredentials());
        } else {
            user = userService.findUserById(userId);
        }
        UserDto userDto;
        if (user.isPresent()) {
            userDto = new UserDto();

            userDto.setId(user.get().getUserId());
            userDto.setUsername(user.get().getUserName());
            userDto.setEmail(user.get().getEmail());
        } else {
            throw new RuntimeException("Пользователь не найден");
        }

        modelAndView.addObject("user", userDto);

        return modelAndView;
    }
    @GetMapping("/userProfile{email}")
    public String userProfile(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        return "redirect:/user?userId=%s".formatted(user.getUserId());
    }
}

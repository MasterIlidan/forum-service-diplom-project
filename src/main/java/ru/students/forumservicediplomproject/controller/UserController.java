package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.service.UserService;
import ru.students.forumservicediplomproject.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ModelAndView users(Model model) {
        ModelAndView modelAndView = new ModelAndView("users-list");
        List<UserDto> users = userService.findAllUsers();
        //List<Role> roles = roleRepository.findAll();

        UserServiceImpl.UserRoles[] roleNames = UserServiceImpl.UserRoles.values();

        modelAndView.addObject("roleNames", roleNames);
        modelAndView.addObject("users", users);
        //modelAndView.addObject("roles", roles);
        return modelAndView;
    }

    @GetMapping("/user/{userId}")
    public ModelAndView userProfile(@PathVariable Long userId, Model model) {
        ModelAndView modelAndView = new ModelAndView("profile-page");
        Optional<User> user;
        if (userId == null) {
            user = Optional.of(userService.getCurrentUserCredentials());
        } else {
            user = userService.findUserById(userId);
        }
        UserDto userDto;
        if (user.isPresent()) {
            User userObj = user.get();
            userDto = new UserDto();

            userDto.setId(userObj.getUserId());
            userDto.setUsername(userObj.getUserName());
            userDto.setEmail(userObj.getEmail());
            userDto.setRegistrationDate(userObj.getRegistrationDate());
        } else {
            throw new RuntimeException("Пользователь не найден");
        }

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("user", userDto);

        return modelAndView;
    }
    @GetMapping("/user/{email}")
    public String userProfile(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        return "redirect:/user?userId=%s".formatted(user.getUserId());
    }

    @PostMapping("/user/")
    public String changeUserRole(@ModelAttribute UserDto userUpdate) {
        if (userUpdate == null) return "redirect:/users";
        Optional<User> optionalUser = userService.findUserById(userUpdate.getId());
        User existingUser;
        if (optionalUser.isPresent()) {
            existingUser = optionalUser.get();
        } else {
            return "redirect:/users";
        }
        List<Role> roles = existingUser.getRoles();
        //roles.set(0, new Role(userUpdate.getRole()));
        existingUser.setRoles(existingUser.getRoles());
        //List<Role> roles = new ArrayList<>();

        //Role role = new Role(requestRoles);
        //roles.add(role);
        //user.setRoles(roles);
        userService.saveUser(existingUser);
        return "redirect:/users";
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

    @GetMapping("/user/{id}/editForm")
    public ModelAndView updateUser(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("user-edit-form");
        Optional<User> optionalUser = userService.findUserById(id);
        User user = new User();
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        UserDto userUpdate = new UserDto();

        userUpdate.setId(user.getUserId());
        userUpdate.setUsername(user.getUserName());
        userUpdate.setEmail(user.getEmail());
        //userUpdate.setRoles(user.getRoles().get(0).getRoleName());
        //String[] roleNames = {"ADMIN", "USER", "READ_ONLY"};
        List<String> roleNames = List.of(new String[]{"ADMIN", "USER", "READ_ONLY"});

        mav.addObject("roleNames", roleNames);
        mav.addObject("user", userUpdate);
        return mav;
    }
}

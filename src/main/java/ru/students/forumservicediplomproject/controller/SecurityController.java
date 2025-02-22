package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.repository.RoleRepository;
import ru.students.forumservicediplomproject.service.UserService;

import java.util.List;

@Controller
public class SecurityController {

    private final RoleRepository roleRepository;
    private final UserService userService;

    public SecurityController(UserService userService,
                              RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }


    @GetMapping("login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "/register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
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

    @GetMapping("/users")
    public String users(Model model) {
        List<UserDto> users = userService.findAllUsers();
        List<Role> roles = roleRepository.findAll();

        String[] roleNames = {"ADMIN", "USER", "READ_ONLY"};

        model.addAttribute("roleNames", roleNames);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "/users";
    }

    @GetMapping("/roles")
    public List<Role> roles() {
        return roleRepository.findAll();
    }

/*  @PostMapping("/userUpdate")
    public String changeUserRole(@ModelAttribute UserUpdate userUpdate) {
        if (userUpdate == null) return "redirect:/users";
        Optional<User> optionalUser = userService.findUserById(userUpdate.getId());
        User existingUser;
        if (optionalUser.isPresent()) {
            existingUser = optionalUser.get();
        } else {
            return "redirect:/users";
        }
        List<Role> roles = existingUser.getRoles();
        roles.set(0, new Role(userUpdate.getRole()));
        existingUser.setRoles(existingUser.getRoles());
        //List<Role> roles = new ArrayList<>();

        //Role role = new Role(requestRoles);
        //roles.add(role);
        //user.setRoles(roles);
        userService.saveUser(existingUser);
        return "redirect:/users";
    }

    @GetMapping("/userUpdate{id}")
    public ModelAndView updateUser(@RequestParam @PathVariable Long id) {
        ModelAndView mav = new ModelAndView("user-edit-form");
        Optional<User> optionalUser = userService.findUserById(id);
        User user = new User();
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        UserUpdate userUpdate = new UserUpdate();

        userUpdate.setId(user.getId());
        userUpdate.setUsername(user.getUsername());
        userUpdate.setEmail(user.getEmail());
        userUpdate.setRole(user.getRoles().get(0).getRoleName());
        //String[] roleNames = {"ADMIN", "USER", "READ_ONLY"};
        List<String> roleNames = List.of(new String[]{"ADMIN", "USER", "READ_ONLY"});

        mav.addObject("roleNames", roleNames);
        mav.addObject("user", userUpdate);
        return mav;
    }*/
    @GetMapping("/about")
    public String about(){
        return "/about";
    }

}

package ru.students.forumservicediplomproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.students.forumservicediplomproject.entity.Role;
import ru.students.forumservicediplomproject.repository.RoleRepository;

import java.util.List;

@Controller
public class SecurityController {

    private final RoleRepository roleRepository;

    public SecurityController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    @GetMapping("login")
    public String login() {
        return "login";
    }


    @GetMapping("/roles")
    public List<Role> roles() {
        return roleRepository.findAll();
    }


    @GetMapping("/about")
    public String about(){
        return "/about";
    }

}

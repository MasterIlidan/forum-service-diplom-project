package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.service.ForumService;
import ru.students.forumservicediplomproject.service.ThreadService;
import ru.students.forumservicediplomproject.service.UserService;

import java.util.Optional;

@Controller
public class ThreadController {
    @Autowired
    private final ThreadService threadService;
    private final ForumService forumServiceImpl;
    private final UserService userService;

    public ThreadController(ForumService forumService,
                            ThreadService threadService,
                            UserService userService) {
        this.forumServiceImpl = forumService;
        this.threadService = threadService;
        this.userService = userService;
    }
    @GetMapping({"/forum/{forumId}/createThread"})
    public ModelAndView createNewThread(@PathVariable long forumId) {
        ModelAndView modelAndView = new ModelAndView("forms/add-thread-page");
        modelAndView.addObject("thread", new ru.students.forumservicediplomproject.entity.Thread());
        modelAndView.addObject("onForumIdCreated", forumId);
        return modelAndView;
    }
    @PostMapping({"/forum/{forumId}/saveThread"})
    public String saveThread(@PathVariable long forumId,
                             @Valid @ModelAttribute("thread") ThreadDto threadDto,
                             BindingResult bindingResult, Model model) {
        User currentUser = userService.getCreatorUserCredentials();
        threadDto.setCreatedBy(currentUser);
        if (bindingResult.hasErrors()) {
            return "forms/add-thread-page";
        }
        ru.students.forumservicediplomproject.entity.Thread thread = new Thread();
        thread.setThreadName(threadDto.getThreadName());
        thread.setCreatedBy(currentUser);
        Optional<Forum> forum = forumServiceImpl.getForum(forumId);
        if (forum.isPresent()) {
            thread.setForumId(forum.get());
        } else {
            throw new RuntimeException(("При создании ветки произошла ошибка:" +
                    " не найден форум, на котором создается ветка. ForumId %s").formatted(threadDto.getForumId()));
        }
        threadService.saveThread(thread);
        return "redirect:/forum?forumId={forumId}";

    }
}

package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.service.ForumServiceImpl;
import ru.students.forumservicediplomproject.service.ThreadService;
import ru.students.forumservicediplomproject.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;
    @Autowired
    private ThreadService threadService;

    public ForumController(ForumServiceImpl forumServiceImpl) {
        this.forumServiceImpl = forumServiceImpl;
    }
    @Autowired
    private UserServiceImpl userService;

    @GetMapping({"/"})
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        //TODO: вернуть список форумов из репозитория
        List<Forum> forumsList = forumServiceImpl.getAllForums();

        List<Object[]> totalThread = forumServiceImpl.countTotalThreads();
        modelAndView.addObject("forumsList", forumsList);
        modelAndView.addObject("threadCount",totalThread.size());
        return modelAndView;
    }

    @GetMapping({"/forum{forumId}"})
    public ModelAndView forumPage(@PathVariable @RequestParam long forumId, Model model) {
        ModelAndView modelAndView = new ModelAndView("forum-page");
        //TODO: вернуть список веток из репозитория
        List<Thread> threadList = threadService.getAllThreadsByForum(forumId);
        modelAndView.addObject("threadList", threadList);

        Optional<Forum> forum = forumServiceImpl.getForum(forumId);
        if (forum.isPresent()) {
            modelAndView.addObject("forum", forum.get());
        } else {
            throw new RuntimeException("У ветки не найден форум! ForumId %s".formatted(forumId));
        }
        return modelAndView;
    }
    @GetMapping({"/forum/thread{threadId}"})
    public ModelAndView threadPage(@PathVariable long threadId) {
        ModelAndView modelAndView = new ModelAndView("thread-page");
        //TODO: вернуть список веток из репозитория
        List<Post> postList = new ArrayList<>();
        modelAndView.addObject("posts", postList);
        return modelAndView;
    }
    @GetMapping({"/forum/thread/post{postId}"})
    public ModelAndView postPage(@PathVariable long postId) {
        ModelAndView modelAndView = new ModelAndView("post");
        //TODO: вернуть список веток из репозитория
        List<Message> postList = new ArrayList<>();
        modelAndView.addObject("posts", postList);
        return modelAndView;
    }
    @GetMapping({"/forum/createForum"})
    public ModelAndView createNewForum() {
        ModelAndView modelAndView = new ModelAndView("forms/add-forum-page");
        modelAndView.addObject("forum", new Forum());
        return modelAndView;
    }
    @PostMapping({"/forum/saveForum"})
    public String saveForum(@Valid @ModelAttribute("forum") ForumDto forumDto,
                            BindingResult bindingResult, Model model) {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        User currentUserr =  userService.findUserByEmail(currentUser.getUsername());
        forumDto.setCreatedBy(currentUserr);

        if (bindingResult.hasErrors()) {
            return "forms/add-forum-page";
        }
        Forum forum = new Forum();

        forum.setForumName(forumDto.getForumName());
        forum.setDescription(forumDto.getDescription());
        forum.setCreatedBy(forumDto.getCreatedBy());
        forumServiceImpl.saveForum(forum);
        return "redirect:/";
    }
    @GetMapping({"/forum{forumId}/createThread"})
    public ModelAndView createNewThread(@PathVariable @RequestParam long forumId) {
        ModelAndView modelAndView = new ModelAndView("forms/add-thread-page");
        modelAndView.addObject("thread", new Thread());
        modelAndView.addObject("forumId", forumId);
        return modelAndView;
    }

}

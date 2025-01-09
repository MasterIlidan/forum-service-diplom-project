package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.service.ForumServiceImpl;
import ru.students.forumservicediplomproject.service.ThreadService;
import ru.students.forumservicediplomproject.service.ThreadServiceImpl;
import ru.students.forumservicediplomproject.service.UserServiceImpl;

import java.util.*;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private ThreadServiceImpl threadServiceImpl;

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



        HashMap<Long,Long> totalThreadsInForum = new HashMap<>();

        for (Forum forum:forumsList) {
            List<Object[]> totalThread = forumServiceImpl.countTotalThreads(forum.getForumId());
            totalThreadsInForum.put(forum.getForumId(), (long) totalThread.get(0)[1]);
        }

        modelAndView.addObject("forumsList", forumsList);
        modelAndView.addObject("threadCountMap",totalThreadsInForum);
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
        List<Post> postList = new ArrayList<>();
        modelAndView.addObject("posts", postList);
        return modelAndView;
    }
    //TODO: сделать отображение и создание постов
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
        User currentUser = getCreatorUserCredentials();
        forumDto.setCreatedBy(currentUser);

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
    @GetMapping({"/forum/{forumId}/createThread"})
    public ModelAndView createNewThread(@PathVariable long forumId) {
        ModelAndView modelAndView = new ModelAndView("forms/add-thread-page");
        modelAndView.addObject("thread", new Thread());
        modelAndView.addObject("onForumIdCreated", forumId);
        return modelAndView;
    }
    @PostMapping({"/forum/{forumId}/saveThread"})
    public String saveThread(@PathVariable long forumId,
                             @Valid @ModelAttribute("thread") ThreadDto threadDto,
                             BindingResult bindingResult, Model model) {
        User currentUser = getCreatorUserCredentials();
        threadDto.setCreatedBy(currentUser);
        if (bindingResult.hasErrors()) {
            return "forms/add-thread-page";
        }
        Thread thread = new Thread();
        thread.setThreadName(threadDto.getThreadName());
        thread.setCreatedBy(currentUser);
        Optional<Forum> forum = forumServiceImpl.getForum(forumId);
        if (forum.isPresent()) {
            thread.setForumId(forum.get());
        } else {
            throw new RuntimeException(("При создании ветки произошла ошибка:" +
                    " не найден форум, на котором создается ветка. ForumId %s").formatted(threadDto.getForumId()));
        }
        threadServiceImpl.saveThread(thread);
        return "redirect:/forum?forumId={forumId}";

    }

    private User getCreatorUserCredentials() {
        org.springframework.security.core.userdetails.User currentUser =
                (org.springframework.security.core.userdetails.User) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();
        return userService.findUserByEmail(currentUser.getUsername());
    }

}

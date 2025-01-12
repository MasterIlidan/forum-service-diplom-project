package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashMap;
import java.util.List;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;
    private final ThreadService threadService;

    public ForumController(ForumServiceImpl forumServiceImpl, ThreadService threadService) {
        this.forumServiceImpl = forumServiceImpl;
        this.threadService = threadService;
    }

    @Autowired
    private UserServiceImpl userService;

    @GetMapping({"/"})
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        List<Forum> forumsList = forumServiceImpl.getAllForums();

        HashMap<Long, Long> totalThreadsInForum = new HashMap<>();
        HashMap<Long, Long> totalPostsInForum = new HashMap<>();
        //TODO: подсчет количества сообщений
        //TODO: когда было последнее сообщение

        //считаем количество веток для каждого форума
        for (Forum forum : forumsList) {
            List<Object[]> totalThread = forumServiceImpl.countTotalThreads(forum.getForumId());
            totalThreadsInForum.put(forum.getForumId(), (long) totalThread.get(0)[1]);
            List<Thread> threadList = threadService.getAllThreadsByForum(forum.getForumId());
            long threads = 0;
            for (Thread thread: threadList) {
                List<Object[]> totalPost = threadService.countTotalPosts(thread.getThreadId());
                threads+=(long) totalPost.get(0)[1];
            }
            totalPostsInForum.put(forum.getForumId(),threads);
        }

        //считаем количество тем

        modelAndView.addObject("forumsList", forumsList);
        modelAndView.addObject("threadCountMap", totalThreadsInForum);
        modelAndView.addObject("postCountMap", totalPostsInForum);
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
        User currentUser = userService.getCurrentUserCredentials();
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


}

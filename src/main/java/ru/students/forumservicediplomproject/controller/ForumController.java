package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.service.ForumServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;

    public ForumController(ForumServiceImpl forumServiceImpl) {
        this.forumServiceImpl = forumServiceImpl;
    }

    @GetMapping({"/"})
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        //TODO: вернуть список форумов из репозитория
        List<Forum> forumsList = forumServiceImpl.getAllForums();
        modelAndView.addObject("forumsList", forumsList);
        return modelAndView;
    }

    @GetMapping({"/forum{forumId}"})
    public ModelAndView forumPage(@PathVariable long forumId) {
        ModelAndView modelAndView = new ModelAndView("forum-page");
        //TODO: вернуть список веток из репозитория
        List<Thread> threadList = new ArrayList<>();
        modelAndView.addObject("threads", threadList);
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

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
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.ForumDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.service.*;

import java.util.List;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;
    private final PostService postService;
    private final StatisticService statisticService;

    public ForumController(ForumServiceImpl forumServiceImpl, PostService postService, StatisticService statisticService) {
        this.forumServiceImpl = forumServiceImpl;
        this.postService = postService;
        this.statisticService = statisticService;
    }

    @GetMapping({"/"})
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        List<Forum> forumsList = forumServiceImpl.getAllForums();

        modelAndView.addObject("lastPost", postService.getLastCreatedPost());
        modelAndView.addObject("statistics", statisticService.getTrackerStatistics());

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("forumsList", forumsList);
        return modelAndView;
    }


    @GetMapping({"/forum/createForum"})
    public ModelAndView createNewForum() {
        ModelAndView modelAndView = new ModelAndView("forms/add-forum-page");

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("forum", new ForumDto());
        return modelAndView;
    }

    @PostMapping({"/forum/saveForum"})
    public String saveForum(@Valid @ModelAttribute("forum") ForumDto forumDto,
                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "forms/add-forum-page";
        }

        forumServiceImpl.saveForum(forumDto);
        return "redirect:/";
    }
    @GetMapping("forum/{forumId}/deleteForum")
    public String deleteForum(@PathVariable long forumId) {
        forumServiceImpl.deleteForum(forumId);
        return "redirect:/";
    }


}

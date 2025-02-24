package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.service.ForumService;
import ru.students.forumservicediplomproject.service.ThreadService;

import java.util.List;

@Slf4j
@Controller
public class ThreadController {
    private final ThreadService threadService;
    private final ForumService forumServiceImpl;

    public ThreadController(ForumService forumService,
                            ThreadService threadService) {
        this.forumServiceImpl = forumService;
        this.threadService = threadService;
    }

    @GetMapping({"/forum/{forumId}"})
    public ModelAndView forumPage(@PathVariable long forumId,
                                  Model model) {
        ModelAndView modelAndView = new ModelAndView("forum-page");

        Forum forum = forumServiceImpl.getForum(forumId);

        List<Thread> threadList = threadService.getAllThreadsByForum(forum);

        modelAndView.addObject("forum", forum);
        modelAndView.addObject("threadList", threadList);

        modelAndView.addObject("search", new Search());

        return modelAndView;
    }

    @GetMapping({"/forum/{forumId}/createThread"})
    public ModelAndView createNewThread(@PathVariable long forumId) {
        ModelAndView modelAndView = new ModelAndView("forms/add-thread-page");

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("thread", new ru.students.forumservicediplomproject.entity.Thread());
        modelAndView.addObject("onForumIdCreated", forumId);
        return modelAndView;
    }

    @PostMapping({"/forum/{forumId}"})
    public String saveThread(@PathVariable long forumId,
                             @Valid @ModelAttribute("thread") ThreadDto threadDto,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "forms/add-thread-page";
        }
        Forum forum;
        try {
            forum = forumServiceImpl.getForum(forumId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("При создании ветки произошла ошибка:" +
                    " не найден форум, на котором создается ветка. ForumId %s".formatted(threadDto.getForumId()));
        }
        threadService.saveThread(threadDto, forum);
        return "redirect:/forum/{forumId}";

    }

    @DeleteMapping("/forum/{forumId}/thread/{threadId}")
    public String deleteThread(@PathVariable long forumId, @PathVariable  long threadId) {
        Thread thread;
        try {
            thread = threadService.getThreadById(threadId);
        } catch (ResourceNotFoundException e) {
            log.error("Ветка для удаления не найдена! Id {}", threadId, e);
            throw new ResourceNotFoundException(e.getMessage());
        }
        threadService.deleteThread(thread);
        return "redirect:/forum/{forumId}";
    }
}

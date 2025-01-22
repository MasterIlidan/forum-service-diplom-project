package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.ThreadDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
public class ThreadController {
    private final ThreadService threadService;
    private final ForumService forumServiceImpl;
    private final PostService postService;
    private final MessageService messageService;

    public ThreadController(ForumService forumService,
                            ThreadService threadService,
                            PostService postService, MessageService messageService) {
        this.forumServiceImpl = forumService;
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
    }

    @GetMapping({"/forum{forumId}"})
    public ModelAndView forumPage(@PathVariable @RequestParam long forumId,
                                  Model model) {
        ModelAndView modelAndView = new ModelAndView("forum-page");
        List<Thread> threadList = threadService.getAllThreadsByForum(forumId);
        modelAndView.addObject("threadList", threadList);

        Optional<Forum> forum = forumServiceImpl.getForum(forumId);
        if (forum.isPresent()) {
            modelAndView.addObject("forum", forum.get());
        } else {
            throw new RuntimeException("У ветки не найден форум! ForumId %s".formatted(forumId));
        }

        HashMap<Long, Long> totalPostsInThread = new HashMap<>();
        HashMap<Long, Long> totalMessagesInThread = new HashMap<>();
        //TODO: когда было последнее сообщение

        //считаем количество веток, тем и сообщений для каждого форума

        for (Thread thread : threadList) {
            long postCount = 0;
            long messageCount = 0;

            List<Object[]> totalPost = postService.countPostsByThread(thread);
            postCount += (long) totalPost.get(0)[1];

            List<Post> postList = postService.getAllPostsByThread(thread);
            for (Post post : postList) {
                List<Object[]> totalMessages = messageService.countMessagesByPost(post);
                messageCount += (long) totalMessages.get(0)[1];
            }
            totalPostsInThread.put(thread.getThreadId(), postCount);
            totalMessagesInThread.put(thread.getThreadId(), messageCount);
        }

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("postCountMap", totalPostsInThread);
        modelAndView.addObject("messagesCountMap", totalMessagesInThread);


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

    @PostMapping({"/forum/{forumId}/saveThread"})
    public String saveThread(@PathVariable long forumId,
                             @Valid @ModelAttribute("thread") ThreadDto threadDto,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "forms/add-thread-page";
        }

        threadService.saveThread(threadDto, forumId);
        return "redirect:/forum?forumId={forumId}";

    }
}

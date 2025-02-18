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
import ru.students.forumservicediplomproject.entity.LastMessage;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.service.*;

import java.util.HashMap;
import java.util.List;

@Controller
public class ForumController {
    private final ForumServiceImpl forumServiceImpl;
    private final ThreadService threadService;
    private final PostService postService;
    private final MessageService messageService;
    private final StatisticService statisticService;
    private final LastMessageService lastMessageService;

    public ForumController(ForumServiceImpl forumServiceImpl, ThreadService threadService, PostService postService, MessageService messageService, StatisticService statisticService, LastMessageService lastMessageService) {
        this.forumServiceImpl = forumServiceImpl;
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
        this.statisticService = statisticService;
        this.lastMessageService = lastMessageService;
    }

    @GetMapping({"/"})
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView("index");
        List<Forum> forumsList = forumServiceImpl.getAllForums();

        HashMap<Long, Long> totalThreadsInForum = new HashMap<>();
        HashMap<Long, Long> totalPostsInForum = new HashMap<>();
        HashMap<Long, Long> totalMessagesInForum = new HashMap<>();
        HashMap<Forum, LastMessage> lastMessageOnForumHashMap = lastMessageService.getAllLastMessagesByForums(forumsList);

        //считаем количество веток, тем и сообщений для каждого форума
        for (Forum forum : forumsList) {
            List<Object[]> totalThread = threadService.countTotalThreadsByForum(forum);
            totalThreadsInForum.put(forum.getForumId(), (long) totalThread.get(0)[1]);

            List<Thread> threadList = threadService.getAllThreadsByForum(forum);
            long postCount = 0;
            long messageCount = 0;
            for (Thread thread : threadList) {
                List<Object[]> totalPost = postService.countPostsByThread(thread);
                postCount += (long) totalPost.get(0)[1];

                List<Post> postList = postService.getAllPostsByThread(thread);
                for (Post post : postList) {
                    List<Object[]> totalMessages = messageService.countMessagesByPost(post);
                    messageCount += (long) totalMessages.get(0)[1];
                }
            }
            totalPostsInForum.put(forum.getForumId(), postCount);
            totalMessagesInForum.put(forum.getForumId(), messageCount);
        }

        modelAndView.addObject("lastPost", postService.getLastCreatedPost());
        modelAndView.addObject("lastMessageOnForum", lastMessageOnForumHashMap);
        modelAndView.addObject("statistics", statisticService.getTrackerStatistics());

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("forumsList", forumsList);
        modelAndView.addObject("threadCountMap", totalThreadsInForum);
        modelAndView.addObject("postCountMap", totalPostsInForum);
        modelAndView.addObject("messagesCountMap", totalMessagesInForum);
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

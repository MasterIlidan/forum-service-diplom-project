package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PostService;
import ru.students.forumservicediplomproject.service.ThreadService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {

    private final ThreadService threadService;

    private final PostService postService;
    private final MessageService messageService;

    public PostController(ThreadService threadService,
                          PostService postService, MessageService messageService) {
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
    }


    @GetMapping({"/forum/{forumId}/thread/{threadId}/posts"})
    public ModelAndView getAllPosts(@PathVariable long forumId,
                                    @PathVariable long threadId, Model model) {
        ModelAndView modelAndView = new ModelAndView("thread-page");
        Optional<Thread> thread = threadService.getThreadById(threadId);
        List<Post> postList;
        if (thread.isPresent()) {
            postList = postService.getAllPostsByThread(thread.get());
        } else {
            throw new RuntimeException("Ветка поста не найдена! ThreadId %s".formatted(threadId));
        }

        HashMap<Long, Long> totalMessagesInPost = new HashMap<>();

        for (Post post : postList) {
            long messageCount = 0;
            List<Object[]> totalMessages = messageService.countMessagesByPost(post);
            messageCount += (long) totalMessages.get(0)[1];
            totalMessagesInPost.put(post.getPostId(), messageCount);
        }


        modelAndView.addObject("postList", postList);
        modelAndView.addObject("messagesCountMap", totalMessagesInPost);
        modelAndView.addObject("forumId", forumId);
        modelAndView.addObject("thread", thread.get());
        modelAndView.addObject("threadId", threadId);
        return modelAndView;
    }

    @GetMapping({"/forum/{forumId}/thread/{threadId}/createPost"})
    public ModelAndView createNewPost(@PathVariable long forumId,
                                      @PathVariable long threadId, Model model) {
        ModelAndView modelAndView = new ModelAndView("forms/add-post-page");

        modelAndView.addObject("post", new PostDto());

        modelAndView.addObject("onForumIdCreated", forumId);
        modelAndView.addObject("onThreadIdCreated", threadId);
        return modelAndView;
    }

    @PostMapping({"/forum/{forumId}/thread/{threadId}/savePost"})
    public String savePost(@PathVariable long forumId,
                           @PathVariable long threadId,
                           @Valid @ModelAttribute("post") PostDto postDto,
                           @RequestParam("torrentFile") MultipartFile torrentFile,
                           BindingResult result,
                           Model model) throws IOException {
        if (result.hasErrors()) {
            return "forms/add-post-page";
        }


        long postId = postService.savePost(torrentFile, postDto, threadId, forumId);
        //Описание раздачи становится первым сообщением в теме
        //model.addAttribute("msg", "Uploaded torrent file hash: " + hash);


        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }
}

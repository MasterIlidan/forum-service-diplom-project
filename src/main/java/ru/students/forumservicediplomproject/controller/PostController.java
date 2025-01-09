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
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.service.PostService;
import ru.students.forumservicediplomproject.service.ThreadService;
import ru.students.forumservicediplomproject.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {

    private final ThreadService threadService;

    private final PostService postService;
    private final UserService userService;

    public PostController(ThreadService threadService,
                          PostService postService, UserService userService) {
        this.threadService = threadService;
        this.postService = postService;
        this.userService = userService;
    }


    @GetMapping({"/forum/{forumId}/thread/{threadId}"})
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
        modelAndView.addObject("postList", postList);

        modelAndView.addObject("forumId", forumId);
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
                           @Valid @ModelAttribute("post") PostDto postDto, BindingResult result,
                           Model model) {
        User currentUser = userService.getCreatorUserCredentials();

        if (result.hasErrors()) {
            return "forms/add-post-page";
        }

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setCreatedBy(currentUser);
        Optional<Thread> thread = threadService.getThreadById(threadId);

        if (thread.isPresent()) {
            post.setThread(thread.get());
        } else {
            throw new RuntimeException("Ветка поста не найдена! PostId %s ForumId %s".formatted(threadId, forumId));
        }
        postService.savePost(post);

        Message message = new Message();
        message.setMessageBy(currentUser);
        message.setMessageBody(postDto.getMessageBody());
        message.setPostId(post);
        //messageService.save(message);
        return "redirect:/forum/%s/thread/%s".formatted(forumId, threadId);
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
}

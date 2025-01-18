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
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PostService;

import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {

    private final PostService postService;
    private final MessageService messageService;

    public MessageController(PostService postService, MessageService messageService) {
        this.postService = postService;
        this.messageService = messageService;
    }

    //TODO: сделать отображение и создание сообщений
    @GetMapping({"/forum/{forumId}/thread/{threadId}/post/{postId}"})
    public ModelAndView postPage(@PathVariable long forumId,
                                 @PathVariable long threadId,
                                 @PathVariable long postId, Model model) {
        ModelAndView modelAndView = new ModelAndView("post");
        //TODO: вернуть список веток из репозитория
        Optional<Post> post = postService.getPostById(postId);
        List<Message> messageList;
        if (post.isPresent()) {
            messageList = messageService.getAllMessagesByPost(post.get());
        } else {
            throw new RuntimeException("Пост не найден! PostId %s".formatted(postId));
        }

        modelAndView.addObject("messages", messageList);
        modelAndView.addObject("post", post.get());

        modelAndView.addObject("newMessage", new Message());



        return modelAndView;
    }
    @PostMapping({"/forum/{forumId}/thread/{threadId}/post/{postId}"})
    public String saveMessage(@PathVariable long forumId,
                                    @PathVariable long threadId,
                                    @PathVariable long postId,
                                    @Valid @ModelAttribute("newMessage") MessageDto messageDto,
                                    BindingResult result) {

        messageService.saveMessage(messageDto, postId);
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }
}


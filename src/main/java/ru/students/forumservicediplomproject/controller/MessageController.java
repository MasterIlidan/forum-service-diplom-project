package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PeersService;
import ru.students.forumservicediplomproject.service.PostService;

import java.util.List;
import java.util.Optional;

@Controller
public class MessageController {

    private final PostService postService;
    private final MessageService messageService;
    private final PeersService peersService;

    public MessageController(PostService postService, MessageService messageService, PeersService peersService) {
        this.postService = postService;
        this.messageService = messageService;
        this.peersService = peersService;
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
        Peers peers = peersService.getPeers(post.get());

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("peers", peers);
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
                                    @RequestParam("image") MultipartFile[] files,
                                    BindingResult result) {

        messageService.saveMessage(messageDto, postId, files);
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }
}


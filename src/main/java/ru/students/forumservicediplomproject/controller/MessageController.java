package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PeersService;
import ru.students.forumservicediplomproject.service.PostService;
import ru.students.forumservicediplomproject.service.ResourceService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class MessageController {

    private final PostService postService;
    private final MessageService messageService;
    private final PeersService peersService;
    private final ResourceService resourceService;

    public MessageController(PostService postService, MessageService messageService, PeersService peersService, ResourceService resourceService) {
        this.postService = postService;
        this.messageService = messageService;
        this.peersService = peersService;
        this.resourceService = resourceService;
    }

    //TODO: сделать отображение и создание сообщений
    @GetMapping({"/forum/{forumId}/thread/{threadId}/post/{postId}"})
    public ModelAndView postPage(@PathVariable long forumId,
                                 @PathVariable long threadId,
                                 @PathVariable long postId, Model model) {
        ModelAndView modelAndView = new ModelAndView("post");
        Optional<Post> post = postService.getPostById(postId);
        List<Message> messageList;

        if (post.isPresent()) {
            messageList = messageService.getAllMessagesByPost(post.get());
            resourceService.getAllResources(messageList);
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
        Optional<Post> postById = postService.getPostById(postId);
        if (postById.isEmpty()) {
            throw new ResourceNotFoundException("Пост не найден! Id %d".formatted(postId));
        }
        messageService.saveMessage(messageDto, postById.get(), files);
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }
}


package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PostService;

import java.util.Optional;

@Slf4j
@Controller
public class MessageController {

    private final PostService postService;
    private final MessageService messageService;

    public MessageController(PostService postService, MessageService messageService) {
        this.postService = postService;
        this.messageService = messageService;
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


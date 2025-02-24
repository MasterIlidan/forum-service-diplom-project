package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Message;
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
        Post postById;
        try {
            postById = postService.getPostById(postId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        messageService.saveMessage(messageDto, postById, files);
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }
    @DeleteMapping("/forum/{forumId}/thread/{threadId}/post/{postId}/{messageId}")
    public String deleteMessage (@PathVariable long forumId,
                                 @PathVariable long threadId,
                                 @PathVariable long postId,
                                 @PathVariable long messageId) {
        Message message;
        try {
            message = messageService.getMessageById(messageId);
        } catch (ResourceNotFoundException e) {
            log.error("Сообщение для удаления не найдено! Id {}", messageId);
            throw new ResourceNotFoundException(e.getMessage());
        }
        messageService.deleteMessage(message);
        return "redirect:/forum/{forumId}/thread/{threadId}/post/{postId}";
    }
}


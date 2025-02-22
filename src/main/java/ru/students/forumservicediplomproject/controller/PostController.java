package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class PostController {

    private final ThreadService threadService;

    private final PostService postService;
    private final MessageService messageService;
    private final PeersService peersService;
    private final ResourceService resourceService;
    private final UserService userService;

    public PostController(ThreadService threadService,
                          PostService postService, MessageService messageService, PeersService peersService, ResourceService resourceService, UserService userService) {
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
        this.peersService = peersService;
        this.resourceService = resourceService;
        this.userService = userService;
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

        HashMap<Post, Long> totalMessagesInPost = new HashMap<>();
        HashMap<Post, Peers> peersHashMap = new HashMap<>(postList.size());
        HashMap<Post, Message> lastMessageInPost = new HashMap<>(postList.size());

        for (Post post : postList) {
            long messageCount = 0;
            List<Object[]> totalMessages = messageService.countMessagesByPost(post);
            messageCount += (long) totalMessages.get(0)[1];
            totalMessagesInPost.put(post, messageCount);

            lastMessageInPost.put(post, messageService.getLastMessageByPost(post));
            peersHashMap.put(post, peersService.getPeers(post));
        }

        modelAndView.addObject("peersHashMap", peersHashMap);

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("lastMessageInPost", lastMessageInPost);
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

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("post", new PostDto());

        modelAndView.addObject("onForumIdCreated", forumId);
        modelAndView.addObject("onThreadIdCreated", threadId);
        return modelAndView;
    }

    @PostMapping({"/forum/{forumId}/thread/{threadId}/savePost"})
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public String savePost(@PathVariable long forumId,
                           @PathVariable long threadId,
                           @Valid @ModelAttribute("post") PostDto postDto,
                           BindingResult result,
                           Model model) {
        if (postDto.getTorrentFile().isEmpty()) {
            result.rejectValue("torrentFile", null, "Нужно загрузить торрент файл");
        }
        if (postDto.getTorrentFile().getOriginalFilename() == null
                || !postDto.getTorrentFile().getOriginalFilename().contains(".torrent")) {
            result.rejectValue("torrentFile", null, "Принимаются только файлы .torrent");
        }
            if (result.hasErrors()) {
                model.addAttribute("post", postDto);
                return "forms/add-post-page";
            }

        Optional<Thread> thread = threadService.getThreadById(threadId);;
        if (thread.isEmpty()) {
            throw new ResourceNotFoundException("Ветка не найдена! Id %d".formatted(threadId));
        }

        long postId = 0;
        try {
            postId = postService.savePost(postDto.getTorrentFile(), postDto, thread.get(), forumId);
        } catch (Exception e) {
            result.rejectValue("torrentFile", null, "Раздача с таким хешем уже зарегистрирована. " +
                    "Попробуйте немного изменить содержимое раздачи (например, добавить/удалить файлы или изменить их имя");
            model.addAttribute("post", postDto);
            return "forms/add-post-page";
        }
        Optional<Post> post = postService.getPostById(postId);
        if (post.isEmpty()) {
            throw new ResourceNotFoundException("Пост не найден! Id %d".formatted(postId));
        }
        messageService.saveMessage(new MessageDto(postDto.getMessageBody()), post.get(), postDto.getImages());
        //Описание раздачи становится первым сообщением в теме
        //model.addAttribute("msg", "Uploaded torrent file hash: " + hash);


        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }

    @PostMapping("/inactivePosts")
    public ResponseEntity inactivePosts(@RequestParam String hash, @RequestParam String status) {
        postService.changePostStatus(hash, status);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/forum/{forumId}/thread/{threadId}/post/{postId}/approvePost")
    public String approvePost(@PathVariable long postId, @PathVariable String forumId,
                              @PathVariable String threadId) {
        postService.approvePost(postId);
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(
                forumId,
                threadId,
                postId
        );
    }

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

        modelAndView.addObject("canDelete", userService.getCurrentUserCredentials().getUserId() == post.get().getPostId());
        modelAndView.addObject("newMessage", new Message());


        return modelAndView;
    }
    @DeleteMapping("/forum/{forumId}/thread/{threadId}/post/{postId}")
    public String deletePost(@PathVariable long forumId, @PathVariable long postId, @PathVariable long threadId) {
        Optional<Post> post = postService.getPostById(postId);
        if (post.isEmpty()) {
            log.error("Пост для удаления не найден! Id {}", postId);
            throw new ResourceNotFoundException("Пост для удаления не найден! Id %d".formatted(postId));
        }
        User currentUserCredentials = userService.getCurrentUserCredentials();
        if (post.get().getCreatedBy().getUserId() != currentUserCredentials.getUserId()) {
            if (!userService.isUserPrivileged(currentUserCredentials)) {
                throw new AccessDeniedException("Недостаточно прав");
            }
        }
        postService.deletePost(post.get());
        return "redirect:/forum/{forumId}/thread/{threadId}/posts";
    }

}

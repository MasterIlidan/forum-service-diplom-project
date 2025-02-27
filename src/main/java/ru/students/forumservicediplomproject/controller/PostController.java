package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.dto.UserDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.exeption.HashAlreadyRegisteredExeption;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.exeption.TrackerServiceException;
import ru.students.forumservicediplomproject.service.*;

import java.nio.file.FileSystem;
import java.util.*;

@Slf4j
@Controller
public class PostController {

    private final ThreadService threadService;

    private final PostService postService;
    private final MessageService messageService;
    private final ResourceService resourceService;
    private final UserService userService;

    public PostController(ThreadService threadService, PostService postService,
                          MessageService messageService, ResourceService resourceService,
                          UserService userService) {
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
        this.resourceService = resourceService;
        this.userService = userService;
    }


    @GetMapping({"/forum/{forumId}/thread/{threadId}/posts"})
    public ModelAndView getAllPosts(@PathVariable long forumId,
                                    @PathVariable long threadId) {
        ModelAndView modelAndView = new ModelAndView("thread-page");
        Thread thread = threadService.getThreadById(threadId);

        List<Post> postList = postService.getAllPostsByThread(thread);

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("postList", postList);
        modelAndView.addObject("thread", thread);
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

        Thread thread = threadService.getThreadById(threadId);

        long postId;
        try {
            postId = postService.savePost(postDto.getTorrentFile(), postDto, thread, forumId);
        } catch (TrackerServiceException e) {
            result.rejectValue("torrentFile", null, "При регистрации торрента произошла ошибка. Попробуйте позже");
            model.addAttribute("post", postDto);
            return "forms/add-post-page";
        } catch (HashAlreadyRegisteredExeption e) {
            result.rejectValue("torrentFile", null, "Раздача с таким хешем уже зарегистрирована. " +
                                                    "Попробуйте немного изменить содержимое раздачи (например, добавить/удалить файлы или изменить их имя");
            model.addAttribute("post", postDto);
            return "forms/add-post-page";
        }
        Post post;
        try {
            post = postService.getPostById(postId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }

        messageService.saveMessage(new MessageDto(postDto.getMessageBody()), post, postDto.getImages());

        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }

    @PostMapping("/inactivePosts")
    public ResponseEntity<String> inactivePosts(@RequestParam String hash, @RequestParam String status) {
        postService.changePostStatus(hash, status);
        return new ResponseEntity<>(HttpStatus.OK);
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
                                 @PathVariable long postId) {
        ModelAndView modelAndView = new ModelAndView("post");
        Post post;
        try {
            post = postService.getPostById(postId);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        List<Message> messageList = messageService.getAllMessagesByPost(post);
        resourceService.getAllMessageResources(messageList);

        UserDto userDto = userService.mapToUserDto(userService.getCurrentUserCredentials());

        modelAndView.addObject("search", new Search());

        modelAndView.addObject("messages", messageList);

        //собрать всех уникальных пользователй и загрузить для них аватар
        Set<User> users = new HashSet<>();
        for (Message message : messageList) {
            users.add(message.getMessageBy());
        }
        for (User user : users) {
            userService.loadUserAvatar(user);
        }

        modelAndView.addObject("post", post);
        modelAndView.addObject("currentUser", userDto);

        modelAndView.addObject("canDelete", userService.getCurrentUserCredentials().getUserId() == post.getPostId());
        modelAndView.addObject("newMessage", new Message());


        return modelAndView;
    }

    @DeleteMapping("/forum/{forumId}/thread/{threadId}/post/{postId}")
    public String deletePost(@PathVariable long forumId, @PathVariable long postId, @PathVariable long threadId) {
        Post post;
        try {
            post = postService.getPostById(postId);
        } catch (ResourceNotFoundException e) {
            log.error("Пост для удаления не найден! Id {}", postId);
            throw new ResourceNotFoundException(e.getMessage());
        }

        User currentUserCredentials = userService.getCurrentUserCredentials();
        if (post.getCreatedBy().getUserId() != currentUserCredentials.getUserId()) {
            if (!userService.isUserPrivileged(currentUserCredentials)) {
                throw new AccessDeniedException("Недостаточно прав");
            }
        }
        postService.deletePost(post);
        return "redirect:/forum/{forumId}/thread/{threadId}/posts";
    }

    @GetMapping("/newPosts")
    public ModelAndView unapprovedPostList() {
        ModelAndView modelAndView = new ModelAndView("unapproved-post-list");

        HashMap<Forum, List<Post>> postList = new HashMap<>(1000);
        for (Post post : postService.getPostsWithNewStatus()) {
            if (!postList.containsKey(post.getThread().getForumId())) {
                postList.put(post.getThread().getForumId(), new ArrayList<>());
            }
            postList.get(post.getThread().getForumId()).add(post);
        }

        modelAndView.addObject("unapprovedPostMap", postList);

        return modelAndView;
    }

    @GetMapping("/forum/{forumId}/thread/{threadId}/post/{postId}/downloadTorrent")
    public ResponseEntity<byte[]> torrentFileDownload(@PathVariable long forumId,
                                                      @PathVariable long threadId,
                                                      @PathVariable long postId) {
        Post post = postService.getPostById(postId);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/download/{hashInfo}";
        Map<String, String> params = Collections.singletonMap("hashInfo", post.getHashInfo());

        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class, params);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(post.getHashInfo()+".torrent").build());

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

    }

}

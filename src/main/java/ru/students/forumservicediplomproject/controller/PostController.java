package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.User;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PostService;
import ru.students.forumservicediplomproject.service.ThreadService;
import ru.students.forumservicediplomproject.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {

    private final ThreadService threadService;

    private final PostService postService;
    private final UserService userService;
    private final MessageService messageService;

    public PostController(ThreadService threadService,
                          PostService postService, UserService userService, MessageService messageService) {
        this.threadService = threadService;
        this.postService = postService;
        this.userService = userService;
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
        modelAndView.addObject("postList", postList);

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
        User currentUser = userService.getCurrentUserCredentials();

        if (result.hasErrors()) {
            return "forms/add-post-page";
        }

        String hash = registerNewTorrent(torrentFile);
        model.addAttribute("msg", "Uploaded torrent file hash: " + hash);

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setCreatedBy(currentUser);
        post.setHashInfo(hash);
        Optional<Thread> thread = threadService.getThreadById(threadId);

        if (thread.isPresent()) {
            post.setThread(thread.get());
        } else {
            throw new RuntimeException("Ветка поста не найдена! PostId %s ForumId %s".formatted(threadId, forumId));
        }
        postService.savePost(post);
        //Описание раздачи становится первым сообщением в теме
        Message message = new Message();
        message.setMessageBy(currentUser);
        message.setMessageBody(postDto.getMessageBody());
        message.setPostId(post);
        messageService.saveMessage(message);


        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, post.getPostId());
    }

    private String registerNewTorrent(MultipartFile torrentFile) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("torrentFile", torrentFile.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8081/register";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        ResponseEntity<String> response = restTemplate.postForEntity(uri1, requestEntity, String.class);

        return response.getBody();
    }

}

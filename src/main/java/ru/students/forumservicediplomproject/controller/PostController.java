package ru.students.forumservicediplomproject.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.LastMessage;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.service.MessageService;
import ru.students.forumservicediplomproject.service.PeersService;
import ru.students.forumservicediplomproject.service.PostService;
import ru.students.forumservicediplomproject.service.ThreadService;

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

    public PostController(ThreadService threadService,
                          PostService postService, MessageService messageService, PeersService peersService) {
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
        this.peersService = peersService;
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
        HashMap<Post, LastMessage> lastMessageOnPostHashMap = messageService.getAllLastMessagesByPost();
        HashMap<Post, Peers> peersHashMap = new HashMap<>(postList.size());

        for (Post post : postList) {
            long messageCount = 0;
            List<Object[]> totalMessages = messageService.countMessagesByPost(post);
            messageCount += (long) totalMessages.get(0)[1];
            totalMessagesInPost.put(post.getPostId(), messageCount);

            peersHashMap.put(post, peersService.getPeers(post));
        }

        modelAndView.addObject("lastMessageOnPost", lastMessageOnPostHashMap);
        modelAndView.addObject("peersHashMap", peersHashMap);

        modelAndView.addObject("search", new Search());

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
    public String savePost(@PathVariable long forumId,
                           @PathVariable long threadId,
                           @Valid @ModelAttribute("post") PostDto postDto,
                           //@RequestParam("torrentFile") MultipartFile torrentFile,
                           //@RequestParam("image") MultipartFile[] images,
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


        long postId = postService.savePost(postDto.getTorrentFile(), postDto, threadId, forumId);
        messageService.saveMessage(new MessageDto(postDto.getMessageBody()), postId, postDto.getImages());
        //Описание раздачи становится первым сообщением в теме
        //model.addAttribute("msg", "Uploaded torrent file hash: " + hash);


        return "redirect:/forum/%s/thread/%s/post/%s".formatted(forumId, threadId, postId);
    }

    @PostMapping("/inactivePosts")
    public ResponseEntity inactivePosts(@RequestParam String hash, @RequestParam String status) {
        postService.changePostStatus(hash, status);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/approvePost{postId}")
    public String approvePost(@PathVariable long postId) {
        postService.approvePost(postId);
        Post postById = postService.getPostById(postId).get();
        return "redirect:/forum/%s/thread/%s/post/%s".formatted(
                postById.getThread().getForumId().getForumId(),
                postById.getThread().getThreadId(),
                postById.getPostId()
        );
    }


}

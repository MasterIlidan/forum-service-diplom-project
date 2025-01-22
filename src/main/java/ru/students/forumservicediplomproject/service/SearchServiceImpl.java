package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final ThreadService threadService;
    private final PostService postService;
    private final MessageService messageService;

    public SearchServiceImpl(ThreadService threadService,
                             PostService postService,
                             MessageService messageService) {
        this.threadService = threadService;
        this.postService = postService;
        this.messageService = messageService;
    }

    @Override
    public List<Thread> threadResult(String keyWord) {
        List<Thread> allThreads = threadService.getAllThreads();
        return allThreads.stream().filter(thread -> thread.getThreadName().equalsIgnoreCase(keyWord) ||
                thread.getThreadName().contains(keyWord)).collect(Collectors.toList());
    }

    @Override
    public List<Post> postResult(String keyWord) {
        List<Post> allThreads = postService.getAllPosts();
        return allThreads.stream().filter(post -> post.getTitle().equalsIgnoreCase(keyWord) ||
                post.getTitle().contains(keyWord)).collect(Collectors.toList());
    }

    @Override
    public List<Message> messageResult(String keyWord) {
        List<Message> allThreads = messageService.getAllMessages();
        return allThreads.stream().filter(message -> message.getMessageBody().equalsIgnoreCase(keyWord) ||
                message.getMessageBody().contains(keyWord)).collect(Collectors.toList());
    }
}

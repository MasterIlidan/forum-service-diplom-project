package ru.students.forumservicediplomproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.LastMessage;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.repository.LastMessageRepository;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    @Autowired
    private  PostService postService;
    private final UserService userService;
    @Autowired
    private LastMessageRepository lastMessageRepository;
    @Autowired
    private ForumService forumService;
    @Autowired
    private ThreadService threadService;


    public MessageServiceImpl(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
    public void saveMessage(MessageDto messageDto, long postId) {
        Optional<Post> post = postService.getPostById(postId);
        Message message = new Message();
        if (post.isPresent()) {
            message.setPostId(post.get());
        } else {
            throw new RuntimeException("Пост не найден! PostId %s".formatted(postId));
        }

        message.setMessageBody(messageDto.getMessageBody());
        message.setMessageBy(userService.getCurrentUserCredentials());
        message.setCreationDate(new Timestamp(new Date().getTime()));
        messageRepository.save(message);

        saveLastMessage(message);

    }

    @Override
    public Optional<Message> getMessageById(long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public void updateMessage(Message message) {
        messageRepository.save(message);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public List<Message> getAllMessagesByPost(Post post) {
        return messageRepository.findAllByPostId(post);
    }
    @Override
    public List<Object[]> countMessagesByPost(Post post) {
        return messageRepository.countTotalMessagesByPostId(post);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
    public void saveLastMessage(Message message) {
        //cохраняем последнее сообщение в посте
        LastMessage lastPostMessage = new LastMessage();
        lastPostMessage.setPost(message.getPostId());
        lastPostMessage.setLastMessage(message);
        lastMessageRepository.deleteByPost(message.getPostId());
        lastMessageRepository.save(lastPostMessage);

        //сохраняем последнее сообщение в ветке
        LastMessage lastThreadMessage = new LastMessage();
        lastThreadMessage.setThread(message.getPostId().getThread());
        lastThreadMessage.setLastMessage(message);
        lastMessageRepository.deleteByThread(message.getPostId().getThread());
        lastMessageRepository.save(lastThreadMessage);

        //сохраняем последнее сообщение в форуме
        LastMessage lastForumMessage = new LastMessage();
        lastForumMessage.setForum(message.getPostId().getThread().getForumId());
        lastForumMessage.setLastMessage(message);
        lastMessageRepository.deleteByForum(message.getPostId().getThread().getForumId());
        lastMessageRepository.save(lastForumMessage);
    }

    @Override
    public LastMessage getLastMessageByPost(Forum forum) {
        return lastMessageRepository.findByForum(forum);
    }

    @Override
    public HashMap<Forum, LastMessage> getAllLastMessagesByForum() {
        List<Forum> allForums = forumService.getAllForums();

        HashMap<Forum, LastMessage> lastMessageHashMap = new HashMap<>(allForums.size());

        for (Forum forum:allForums) {
            LastMessage lastMessage = getLastMessageByPost(forum);
            lastMessageHashMap.put(forum, lastMessage);
        }

        return lastMessageHashMap;
    }

    @Override
    public LastMessage getLastMessageByThread(Thread thread) {
        return lastMessageRepository.findByThread(thread);
    }

    @Override
    public HashMap<Thread, LastMessage> getAllLastMessagesByThread() {
        List<Thread> allThreads = threadService.getAllThreads();

        HashMap<Thread, LastMessage> lastMessageHashMap = new HashMap<>(allThreads.size());

        for (Thread thread:allThreads) {
            LastMessage lastMessage = getLastMessageByThread(thread);
            lastMessageHashMap.put(thread, lastMessage);
        }

        return lastMessageHashMap;
    }

    @Override
    public LastMessage getLastMessageByPost(Post post) {
        return lastMessageRepository.findByPost(post);
    }

    @Override
    public HashMap<Post, LastMessage> getAllLastMessagesByPost() {
        List<Post> allPosts = postService.getAllPosts();

        HashMap<Post, LastMessage> lastMessageHashMap = new HashMap<>(allPosts.size());

        for (Post post:allPosts) {
            LastMessage lastMessage = getLastMessageByPost(post);
            lastMessageHashMap.put(post, lastMessage);
        }

        return lastMessageHashMap;
    }
}

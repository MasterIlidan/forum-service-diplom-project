package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.repository.LastMessageRepository;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    @Autowired
    private PostService postService;
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
    //@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
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

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/registerNewResource";
        URI uri1 = UriComponentsBuilder.fromUriString(url)
                .build().toUri();
        ResponseEntity<String> response = null;

        int retry = 0;
        while (response == null) {
            try {
                response = restTemplate.getForEntity(uri1, String.class);
            } catch (Exception e) {
                retry++;
                log.warn("Не удалось зарегистрировать ресурс. Попытка {}", retry);
                if (retry > 3) {
                    log.error("Ошибка при регистрации ресурса", e);
                    throw e;
                }
            }

        }

        log.info(response.getBody());

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

    /** Сохранить последнее отправленное сообщение в таблице последних сообщений для отображения
     * активности в разных разделах сайта
     * @param message Только что созданное сообщение, которое отправил пользователь.
     */
    @Override
    public void saveLastMessage(Message message) {
        //cохраняем последнее сообщение в посте
            saveLastMessageInPost(message);
        //сохраняем последнее сообщение в ветке
            saveLastMessageInThread(message);
        //сохраняем последнее сообщение в форуме
            saveLastMessageInForum(message);
    }

    /**Последнее сообщение в посту. Время создания сообщения будет
     *  отображаться как последнее созданное сообщение по теме.
     * @param message Только что созданное сообщение, которое отправил пользователь.
     */
    private void saveLastMessageInPost(Message message) {
        LastMessage lastPostMessage = new LastMessage();
        lastPostMessage.setPost(message.getPostId());
        lastPostMessage.setLastMessage(message);
        LastMessage oldMessage = lastMessageRepository.findByPost(message.getPostId());
        if (oldMessage != null) lastMessageRepository.delete(oldMessage);
        lastMessageRepository.save(lastPostMessage);
    }

    /**Последнее сообщение в ветке. Время создания сообщения будет
     *  отображаться как последнее созданное сообщение по ветке.
     * @param message Только что созданное сообщение, которое отправил пользователь.
     */
    private void saveLastMessageInThread(Message message) {
        LastMessage lastThreadMessage = new LastMessage();
        lastThreadMessage.setThread(message.getPostId().getThread());
        lastThreadMessage.setLastMessage(message);
        LastMessage oldMessage = lastMessageRepository.findByThread(message.getPostId().getThread());
        if (oldMessage != null) lastMessageRepository.delete(oldMessage);
        lastMessageRepository.save(lastThreadMessage);
    }
    /**Последнее сообщение в форуме. Время создания сообщения будет
     *  отображаться как последнее созданное сообщение на форуме.
     * @param message Только что созданное сообщение, которое отправил пользователь.
     */
    private void saveLastMessageInForum(Message message) {
        LastMessage lastForumMessage = new LastMessage();
        lastForumMessage.setForum(message.getPostId().getThread().getForumId());
        lastForumMessage.setLastMessage(message);
        LastMessage oldMessage = lastMessageRepository.findByForum(message.getPostId().getThread().getForumId());
        if (oldMessage != null) lastMessageRepository.delete(oldMessage);
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

        for (Forum forum : allForums) {
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

        for (Thread thread : allThreads) {
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

        for (Post post : allPosts) {
            LastMessage lastMessage = getLastMessageByPost(post);
            lastMessageHashMap.put(post, lastMessage);
        }

        return lastMessageHashMap;
    }
}

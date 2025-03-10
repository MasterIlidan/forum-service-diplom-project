package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ResourceService resourceService;

    public MessageServiceImpl(MessageRepository messageRepository, UserService userService,
                              ResourceService resourceService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.resourceService = resourceService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void saveMessage(MessageDto messageDto, Post post, @Nullable MultipartFile[] files) {
        Message message = new Message();

        message.setPostId(post);
        message.setMessageBody(messageDto.getMessageBody());
        message.setMessageBy(userService.getCurrentUserCredentials());
        message.setCreationDate(new Timestamp(new Date().getTime()));

        if (files != null && !Arrays.stream(files).allMatch(MultipartFile::isEmpty)) {
            try {
                List<Resource> content = new ArrayList<>(files.length);
                for (MultipartFile file:files) {
                    Resource resource = resourceService.registerNewResource(file);
                    if (resource == null) {
                        log.error("При регистрации ресурса произошла ошибка. Сохранение без него");
                        continue;
                    }
                    content.add(resource);
                }
                message.setContent(content);
            } catch (Exception e) {
                log.error("Ошибка при регистрации ресурсов. Сообщение сохранено без них", e);
            }
        }
        messageRepository.save(message);

    }


    @Override
    public Message getMessageById(long messageId) {
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isEmpty()) {
            log.error("Сообщение с id {} не найдено!", messageId);
            throw new ResourceNotFoundException("Сообщение с id %d не найдено!".formatted(messageId));
        }
        return message.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteMessage(Message message) {
        if (!message.getContent().isEmpty()) {
            resourceService.removeMessageResources(message);
        }
        messageRepository.deleteById(message.getMessageId());
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
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteAllMessagesByPost(Post post) {
        List<Message> messageList = getAllMessagesByPost(post);
        for (Message message:messageList){
            deleteMessage(message);
        }
    }

    @Nullable
    @Override
    public Message getLastMessageByForum(Forum forum) {
        return messageRepository.findLatestPostByForum(forum.getForumId());
    }
    @Nullable
    @Override
    public Message getLastMessageByThread(Thread thread) {
        return messageRepository.findLatestPostByThread(thread.getThreadId());
    }
    @Nullable
    @Override
    public Message getLastMessageByPost(Post post) {
        return messageRepository.findLatestPostByPost(post.getPostId());
    }

    @Override
    public Long countMessagesByPost(Post post) {
        return messageRepository.countAllByPostId(post);
    }


}

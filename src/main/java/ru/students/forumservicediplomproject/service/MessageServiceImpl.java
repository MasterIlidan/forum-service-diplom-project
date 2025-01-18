package ru.students.forumservicediplomproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    @Autowired
    private  PostService postService;
    private final UserService userService;


    public MessageServiceImpl(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Override
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
}

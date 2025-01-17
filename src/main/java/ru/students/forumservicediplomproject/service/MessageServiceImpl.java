package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;


    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void saveMessage(Message message) {
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

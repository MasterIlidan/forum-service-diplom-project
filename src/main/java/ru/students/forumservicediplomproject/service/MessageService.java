package ru.students.forumservicediplomproject.service;

import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;

import java.util.List;
import java.util.Optional;

public interface MessageService {

    void saveMessage(MessageDto messageDto, Post post, MultipartFile[] files);

    Optional<Message> getMessageById(long id);

    void deleteMessage(Long messageId);

    void updateMessage(Message message);

    List<Message> getAllMessages();

    List<Message> getAllMessagesByPost(Post post);

    void deleteAllMessagesByPost(Post post);

    List<Object[]> countMessagesByPost(Post post);

}

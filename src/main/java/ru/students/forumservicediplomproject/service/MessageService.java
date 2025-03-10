package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.List;

public interface MessageService {

    void saveMessage(MessageDto messageDto, Post post, MultipartFile[] files, boolean isMainMessage);

    Message getMessageById(long id);

    void deleteMessage(Message message);

    void updateMessage(Message message);

    List<Message> getAllMessages();

    List<Message> getAllMessagesByPost(Post post);

    void deleteAllMessagesByPost(Post post);

    @Nullable
    Message getLastMessageByThread(Thread thread);

    @Nullable
    Message getLastMessageByForum(Forum forum);

    @Nullable
    Message getLastMessageByPost(Post post);

    Long countMessagesByPost(Post post);

}

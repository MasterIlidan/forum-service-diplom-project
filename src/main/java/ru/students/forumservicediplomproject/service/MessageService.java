package ru.students.forumservicediplomproject.service;

import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.Forum;
import ru.students.forumservicediplomproject.entity.LastMessage;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface MessageService {

    void saveMessage(MessageDto messageDto, long postId, MultipartFile[] files);

    Optional<Message> getMessageById(long id);

    void deleteMessage(Long messageId);

    void updateMessage(Message message);

    List<Message> getAllMessages();

    List<Message> getAllMessagesByPost(Post post);

    List<Object[]> countMessagesByPost(Post post);

    void saveLastMessage(Message message);

    LastMessage getLastMessageByPost(Forum forum);

    HashMap<Forum, LastMessage> getAllLastMessagesByForum();

    LastMessage getLastMessageByThread(Thread thread);

    HashMap<Thread, LastMessage> getAllLastMessagesByThread();

    LastMessage getLastMessageByPost(Post post);

    HashMap<Post, LastMessage> getAllLastMessagesByPost();
}

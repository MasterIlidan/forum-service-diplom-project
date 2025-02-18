package ru.students.forumservicediplomproject.service;

import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.repository.LastMessageRepository;

import java.util.HashMap;
import java.util.List;

@Service
public class LastMessageServiceImpl implements LastMessageService {
    private final LastMessageRepository lastMessageRepository;

    public LastMessageServiceImpl(LastMessageRepository lastMessageRepository) {
        this.lastMessageRepository = lastMessageRepository;
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
    public HashMap<Forum, LastMessage> getAllLastMessagesByForums(List<Forum> forumList) {

        HashMap<Forum, LastMessage> lastMessageHashMap = new HashMap<>(forumList.size());

        for (Forum forum : forumList) {
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
    public HashMap<Thread, LastMessage> getAllLastMessagesByThreads(List<Thread> threadList) {
        HashMap<Thread, LastMessage> lastMessageHashMap = new HashMap<>(threadList.size());

        for (Thread thread : threadList) {
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
    public HashMap<Post, LastMessage> getAllLastMessagesByPosts(List<Post> postList) {

        HashMap<Post, LastMessage> lastMessageHashMap = new HashMap<>(postList.size());

        for (Post post : postList) {
            LastMessage lastMessage = getLastMessageByPost(post);
            lastMessageHashMap.put(post, lastMessage);
        }

        return lastMessageHashMap;
    }

    @Override
    public void deleteByThread(Thread thread) {
        lastMessageRepository.deleteByThread(thread);
    }

    @Override
    public void deleteByPost(Post post) {
        lastMessageRepository.deleteByPost(post);
    }

    @Override
    public void deleteByForum(Forum forum) {
        lastMessageRepository.deleteByForum(forum);
    }
}

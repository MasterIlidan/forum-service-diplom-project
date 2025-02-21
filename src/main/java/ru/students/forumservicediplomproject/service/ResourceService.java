package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Resource;

import java.util.List;

public interface ResourceService {
    void saveResource(Resource resource);

    void getAllResources(List<Message> messageList);

    void removeMessageResources(Message message);
}

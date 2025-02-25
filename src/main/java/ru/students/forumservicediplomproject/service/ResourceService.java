package ru.students.forumservicediplomproject.service;

import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Resource;

import java.util.List;

public interface ResourceService {

    Resource registerNewResource(MultipartFile file) throws RestClientException;

    void saveResource(Resource resource);

    void getAllMessageResources(List<Message> messageList);


    void getResource(Resource resource);

    void removeMessageResources(Message message);
}

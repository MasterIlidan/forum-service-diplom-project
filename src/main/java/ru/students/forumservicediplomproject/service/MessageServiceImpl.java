package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.entity.*;
import ru.students.forumservicediplomproject.repository.MessageRepository;

import java.net.URI;
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
    //@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void saveMessage(MessageDto messageDto, Post post, @Nullable MultipartFile[] files) {
        Message message = new Message();

        message.setPostId(post);
        message.setMessageBody(messageDto.getMessageBody());
        message.setMessageBy(userService.getCurrentUserCredentials());
        message.setCreationDate(new Timestamp(new Date().getTime()));

        //TODO: уведомление пользователя о неудаче при сохранении сообщения

        if (files != null && !Arrays.stream(files).allMatch(MultipartFile::isEmpty)) {
            try {
                List<String> resources = registerNewResources(message, files);
                List<Resource> content = new ArrayList<>(resources.size());
                for (String uuid:resources) {
                    Resource resource = new Resource();
                    resource.setUuid(uuid);
                    resourceService.saveResource(resource);
                    content.add(resource);
                }
                message.setContent(content);
            } catch (Exception e) {
                log.error("Ошибка при регистрации ресурсов. Сообщение сохранено без них", e);
            }
        }
        messageRepository.save(message);
    }

    private List<String> registerNewResources(Message message, MultipartFile[] files) throws RestClientException {
        files = Arrays.stream(files).filter(file -> !file.isEmpty()).toList().toArray(new MultipartFile[0]);

        log.debug("Регистрация новых ресурсов для сообщения {} количество {}", message.getMessageId(), files.length);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/registerNewResource";
        URI uri1 = UriComponentsBuilder.fromUriString(url)
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file: files) {
            body.add("image", file.getResource());
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<List> response = null;

        int retry = 0;
        while (response == null) {
            try {
                response = restTemplate.postForEntity(uri1, requestEntity, List.class);
            } catch (RestClientException e) {
                retry++;
                log.warn("Не удалось зарегистрировать ресурс. Попытка {}", retry);
                if (retry > 3) {
                    log.error("Ошибка при регистрации ресурса", e);
                    throw new RestClientException("Ошибка при регистрации ресурса", e);
                }
            }

        }
        if (response.getBody() == null) {
            log.warn("Произошла проблема при регистрации ресурсов. Сервис вернул пустой ответ, ожидалось {}", files.length);
            return List.of();
        }
        log.info(Arrays.toString(response.getBody().toArray()));
        return response.getBody();
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
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteAllMessagesByPost(Post post) {
        messageRepository.deleteAllByPostId(post);
    }

    @Override
    public List<Object[]> countMessagesByPost(Post post) {
        return messageRepository.countTotalMessagesByPostId(post);
    }


}

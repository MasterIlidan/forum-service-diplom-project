package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Resource;
import ru.students.forumservicediplomproject.repository.ResourceRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResourceServiceImpl implements ResourceService {
    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void saveResource(Resource resource) {
        resourceRepository.save(resource);
    }

    @Override
    public void getAllResources(List<Message> messageList) {
        for (Message message : messageList) {
            List<Resource> content = message.getContent();
            if (content == null) continue;
            if (content.isEmpty()) continue;

            for (Resource resource:content) {
                RestTemplate restTemplate = new RestTemplate();
                //TODO: настраиваемая ссылка из конфигурации
                String uri = "http://localhost:8082/resource{uuid}";

                Map<String, String> params = Collections.singletonMap("uuid", resource.getUuid());

                ResponseEntity<String> response = null;

                int retry = 0;
                while (response == null) {
                    try {
                        response = restTemplate.getForEntity(uri, String.class, params);
                        log.debug("Получен ответ от сервиса ресурсов: {}, {}", response.getStatusCode(), response.getBody().length());
                    } catch (Exception e) {
                        retry++;
                        log.warn("Не удалось получить ресурс. Попытка {}", retry);
                        if (retry > 3) {
                            log.error("Ошибка при получении ресурса", e);
                            throw e;
                        }
                        continue;
                    }
                    break;
                }
                if (response.getBody() == null) {
                    log.error("Ресурс {} не найден сервисом. MessageId {}", resource.getUuid(), message.getMessageId());
                    continue;
                }
                resource.setBase64Image(response.getBody());
            }
        }
    }

    @Override
    public void removeMessageResources(Message message) {
        RestTemplate restTemplate = new RestTemplate();
        //TODO: настраиваемая ссылка из конфигурации
        String uri = "http://localhost:8082/resource{uuid}";

        for (Resource resource: message.getContent()) {
            Map<String, String> params = Collections.singletonMap("uuid", resource.getUuid());

            for (int retry = 1; retry <= 3; retry++) {
                try {
                    restTemplate.delete(uri, params);
                    break;
                } catch (RestClientException e) {
                    log.warn("Не удалось удалить ресурс {}. Попытка {}", resource.getUuid(),retry, e);
                }
                log.error("Ошибка при удалении ресурса {}", resource.getUuid());
            }
        }

    }

}

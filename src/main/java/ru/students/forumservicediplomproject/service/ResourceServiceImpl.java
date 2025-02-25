package ru.students.forumservicediplomproject.service;

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
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Resource;
import ru.students.forumservicediplomproject.repository.ResourceRepository;

import java.net.URI;
import java.util.Arrays;
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
    public Resource registerNewResource(MultipartFile file) throws RestClientException {
        if (file.isEmpty()) return null;

        //log.debug("Регистрация новых ресурсов для сообщения {} количество {}", message.getMessageId(), files.length);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/resource";
        URI uri1 = UriComponentsBuilder.fromUriString(url)
                .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("image", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = null;

        int retry = 0;
        while (response == null) {
            try {
                response = restTemplate.postForEntity(uri1, requestEntity, String.class);
            } catch (RestClientException e) {
                retry++;
                log.warn("Не удалось зарегистрировать ресурс. Попытка {}", retry);
                if (retry > 3) {
                    log.error("Ошибка при регистрации ресурса", e);
                    throw new RestClientException("Ошибка при регистрации ресурса", e);
                }
            }

        }
/*        if (response.getBody() == null) {
            log.warn("Произошла проблема при регистрации ресурсов. Сервис вернул пустой ответ, ожидалось {}", files.length);
            return List.of();
        }*/
        log.info(response.getBody());
        if (response.getBody() != null) {
            Resource resource = new Resource();
            resource.setUuid(response.getBody());
            //saveResource(resource);
            return resource;
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void saveResource(Resource resource) {
        resourceRepository.save(resource);
    }

    @Override
    public void getAllMessageResources(List<Message> messageList) {
        for (Message message : messageList) {
            List<Resource> content = message.getContent();
            if (content == null) continue;
            if (content.isEmpty()) continue;

            for (Resource resource : content) {
                getResource(resource);
            }
        }
    }

    @Override
    public void getResource(Resource resource) {
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
            log.error("Ресурс {} не найден сервисом.", resource.getUuid());
            return;
        }
        resource.setBase64Image(response.getBody());
    }

    @Override
    public void removeMessageResources(Message message) {
        RestTemplate restTemplate = new RestTemplate();
        //TODO: настраиваемая ссылка из конфигурации
        String uri = "http://localhost:8082/resource{uuid}";

        for (Resource resource : message.getContent()) {
            Map<String, String> params = Collections.singletonMap("uuid", resource.getUuid());

            for (int retry = 1; retry <= 3; retry++) {
                try {
                    restTemplate.delete(uri, params);
                    break;
                } catch (RestClientException e) {
                    log.warn("Не удалось удалить ресурс {}. Попытка {}", resource.getUuid(), retry, e);
                }
                log.error("Ошибка при удалении ресурса {}", resource.getUuid());
            }
        }

    }

}

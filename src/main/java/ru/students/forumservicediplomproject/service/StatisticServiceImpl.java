package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.entity.Statistics;

import java.net.URI;
import java.util.HashMap;

@Slf4j
@Service
public class StatisticServiceImpl implements StatisticService {
    private final PostService postService;
    private final PeersService peersService;

    public StatisticServiceImpl(PostService postService, PeersService peersService) {
        this.postService = postService;
        this.peersService = peersService;
    }

    @Override
    public Statistics getTrackerStatistics() {
        return new Statistics(
                getCountOfAllPeers(),
                getCountOfAllPosts(),
                getAllTrackerSize()

        );
    }

    @Override
    public long getCountOfAllPosts() {
        return postService.getCountOfAllPosts();
    }

    @Override
    public HashMap<String, Long> getCountOfAllPeers() {
        return peersService.getCountOfAllPeers();
    }

    @Override
    public double getAllTrackerSize() {
        RestTemplate restTemplate = new RestTemplate();
        //TODO: настраиваемая ссылка из конфигурации
        String uri = "http://localhost:8081/getSizeOfTracker";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        Double response = null;


        int retry = 0;
        while (response == null) {
            try {
                response = restTemplate.getForObject(uri1, Double.class);
            } catch (Exception e) {
                retry++;
                log.warn("Не удалось получить общий размер раздач на трекере. Попытка {}", retry);
                if (retry > 3) {
                    log.error("Ошибка при запросе общего размера раздач на трекере", e);
                    response = 0d;
                    break;
                }
                continue;
            }
            break;
        }
        double trackerSize;
        if (response == null) {
            trackerSize = 0;
        } else {
            trackerSize = response;
        }
        return trackerSize;
    }
}

package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.repository.PeersRepository;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PeersServiceImpl implements PeersService {
    private final PeersRepository peersRepository;
    private final PostService postService;

    public PeersServiceImpl(PeersRepository peersRepository, PostService postService) {
        this.peersRepository = peersRepository;
        this.postService = postService;
    }

    /**
     * Обновление таблицы PEERS
     */
    @Override
    @Async
    @Scheduled(cron = "*/15 * * * * *")
    public void updatePeers() {
        //TODO: нужна ли возможность отключения "кеша" и постоянного запроса по каждой раздаче?
        log.info("Обновление таблицы раздач и пиров");

        List<Peers> peersList = peersRepository.findAll();

        ResponseEntity<LinkedHashMap> response = getResponseEntity();

        Map<String, LinkedHashMap<String, Integer>> peersMap = response.getBody();

        if (peersMap == null) {
            log.warn("Список пиров null");
            return;
        }

        for (Peers peers : peersList) {
            if (peersMap.containsKey(peers.getPost().getHashInfo())) {
                peers.setLeechers(peersMap.get(peers.getPost().getHashInfo()).get("leechers"));
                peers.setSeeders(peersMap.get(peers.getPost().getHashInfo()).get("seeders"));
                peersMap.remove(peers.getPost().getHashInfo());
            } else {
                peers.setLeechers(0);
                peers.setSeeders(0);
            }
            peersRepository.save(peers);
            log.debug("Сидеров {} личеров {} хеш раздачи {}", peers.getSeeders(), peers.getLeechers(), peers.getPost().getHashInfo());
        }
        for (String hash : peersMap.keySet()) {
            Post post = postService.getPostByHashInfo(hash);

            Peers peers = new Peers();
            peers.setSeeders(peersMap.get(hash).get("seeders"));
            peers.setLeechers(peersMap.get(hash).get("leechers"));
            peers.setPost(post);
            peersRepository.save(peers);
            log.debug("(NEW) Сидеров {} личеров {} хеш раздачи {}", peers.getSeeders(), peers.getLeechers(), peers.getPost().getHashInfo());
        }

        log.info("Обновлено записей {}. Пиры отсутствуют у {} раздач", peersMap.size(), peersList.size() - peersMap.size());
    }

    /**Запрос в сервис трекера и возврат ответа
     *
     * @return Ответ от трекера. Ключ - хеш торрента, значение - тоже мапа с ключами leechers и seeders.
     */
    private ResponseEntity<LinkedHashMap> getResponseEntity() {
        RestTemplate restTemplate = new RestTemplate();
        //TODO: настраиваемая ссылка из конфигурации
        String uri = "http://localhost:8081/getPeers";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        ResponseEntity<LinkedHashMap> response = null;

        int retry = 0;
        while (response == null) {
            try {
                response = restTemplate.getForEntity(uri1, LinkedHashMap.class);
            } catch (Exception e) {
                retry++;
                log.warn("Не удалось обновить таблицу пиров. Попытка {}", retry);
                if (retry > 3) {
                    log.error("Ошибка при обновлении таблицы раздач и пиров", e);
                    throw e;
                }
                continue;
            }
            break;
        }

        return response;
    }

    @Override
    public Peers getPeers(Post post) {
        Peers peers = peersRepository.findByPost(post);
        return peers == null ? new Peers(0, post, 0, 0) : peers;
    }
    @Override
    public HashMap<String, Long> getCountOfAllPeers() {
        List<Peers> peersList = peersRepository.findAll();
        HashMap<String,Long> allPeers = new HashMap<>(2);

        long seeders = 0;
        long leechers = 0;

        for (Peers peers:peersList) {
            seeders += peers.getSeeders();
            leechers += peers.getLeechers();
        }

        allPeers.put("seeders", seeders);
        allPeers.put("leechers", leechers);
        return allPeers;
    }
}

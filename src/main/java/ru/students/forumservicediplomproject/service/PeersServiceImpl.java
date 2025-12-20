package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.config.ExternalServiceUrls;
import ru.students.forumservicediplomproject.dto.PeersDto;
import ru.students.forumservicediplomproject.entity.Peers;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.repository.PeersRepository;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
public class PeersServiceImpl implements PeersService {
    private final PeersRepository peersRepository;

    private final ExternalServiceUrls externalServiceUrls;
    @Value("${openshare.tracker.cahchePeers}")
    private boolean cachePeersEnabled;

    public PeersServiceImpl(PeersRepository peersRepository, PostService postService, ExternalServiceUrls externalServiceUrls) {
        this.peersRepository = peersRepository;
        this.externalServiceUrls = externalServiceUrls;
    }


    //TODO: эту бредятину надо полностью пересмотреть и переделать (как и половину сервиса)
    /**
     * Обновление таблицы PEERS
     */
    @Override
    @Async
    @Scheduled(cron = "0 */5 * * * *")
    public void updatePeers() {
        //TODO: нужна ли возможность отключения "кеша" и постоянного запроса по каждой раздаче?
        log.info("Обновление таблицы раздач и пиров");

        List<Peers> peersList = peersRepository.findAll();

        Map<String, LinkedHashMap<String, Integer>> peersMap = getAllPeersMap();

        if (peersMap == null) {
            log.warn("Список пиров null");
            return;
        }

        for (Peers peers : peersList) {
            if (peersMap.containsKey(peers.getPost().getHashInfo())) {
                peers.setLeechers(peersMap.get(peers.getPost().getHashInfo()).get("leechers"));
                peers.setSeeders(peersMap.get(peers.getPost().getHashInfo()).get("seeders"));
            } else {
                peers.setLeechers(0);
                peers.setSeeders(0);
            }
            peersRepository.save(peers);
            log.debug("Сидеров {} личеров {} хеш раздачи {}", peers.getSeeders(), peers.getLeechers(), peers.getPost().getHashInfo());
        }

        log.info("Обновлено записей {}. Пиры отсутствуют у {} раздач", peersMap.size(), peersList.size() - peersMap.size());
    }

    /**Запрос в сервис трекера и возврат ответа
     *
     * @return Ответ от трекера. Ключ - хеш торрента, значение - тоже мапа с ключами leechers и seeders.
     */
    @Nullable
    private Map<String, LinkedHashMap<String, Integer>> getAllPeersMap() {
        RestTemplate restTemplate = new RestTemplate();
        String uri = externalServiceUrls.getTrackerServiceUrl() + "/getPeers";
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

        return response.getBody();
    }

    private PeersDto getPeers(String hash) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = externalServiceUrls.getTrackerServiceUrl() + "/getPeers/"+hash;
        URI uri1 = UriComponentsBuilder
                .fromUriString(uri)
                .build()
                .toUri();
        PeersDto peers = new PeersDto();
        int retry = 0;
        while (retry <= 3) {
            try {
                ResponseEntity<PeersDto> exchange =
                        restTemplate
                                .exchange(
                                        uri1,
                                        HttpMethod.GET,
                                        null,
                                        new ParameterizedTypeReference<>(){});
                peers = exchange.getBody();
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

        return peers;
    }

    private List<PeersDto> getManyPeers(List<String> hashes) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = externalServiceUrls.getTrackerServiceUrl() + "/getPeers";
        URI uri1 = UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("hashes", hashes)
                .build()
                .toUri();
        List<PeersDto> peersList = new ArrayList<>();
        int retry = 0;
        while (retry <= 3) {
            try {
                ResponseEntity<List<PeersDto>> exchange =
                        restTemplate
                                .exchange(
                                        uri1,
                                        HttpMethod.GET,
                                        null,
                                        new ParameterizedTypeReference<>(){});
                peersList = exchange.getBody();
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

        return peersList;
    }

    @Override
    public Peers getPeersForPost(Post post) {
        if (cachePeersEnabled) {
            Peers peers = peersRepository.findByPost(post);
            return peers == null ? new Peers(0, post, 0, 0) : peers;
        } else {
            PeersDto peersMap = getPeers(post.getHashInfo());
            return new Peers(0, post, peersMap.getLeechers(), peersMap.getSeeders());
        }
    }

    @Override
    public List<Peers> getPeersForPosts(List<Post> postsList) {
        List<String> hashes = postsList.stream().map((Post::getHashInfo)).toList();
        List<PeersDto> manyPeers = getManyPeers(hashes);

        return manyPeers
                .stream()
                .map(
                        (peersDto ->
                                new Peers(
                                        0,
                                        postsList
                                                .stream()
                                                .filter(
                                                        (post ->
                                                                peersDto.getHash().equals(post.getHashInfo())))
                                                .findFirst()
                                                .get(),
                                        peersDto.getLeechers(),
                                        peersDto.getSeeders()))).toList();
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

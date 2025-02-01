package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.students.forumservicediplomproject.entity.Statistics;

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
                getTrackerSize()
        );
    }
    @Override
    public long getCountOfAllPosts() {
        return postService.getCountOfAllPosts();
    }
    @Override
    public long getTrackerSize() {
        log.warn("Not implemented yet");
        return -1;
    }
    @Override
    public HashMap<String, Long> getCountOfAllPeers() {
        return peersService.getCountOfAllPeers();
    }
}

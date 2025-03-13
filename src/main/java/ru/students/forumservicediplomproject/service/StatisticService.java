package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.service.model.Statistics;

import java.util.HashMap;

public interface StatisticService {

    Statistics getTrackerStatistics();

    long getCountOfAllPosts();


    HashMap<String, Long> getCountOfAllPeers();

    double getAllTrackerSize();
}

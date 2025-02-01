package ru.students.forumservicediplomproject.service;

import ru.students.forumservicediplomproject.entity.Statistics;

import java.util.HashMap;

public interface StatisticService {

    Statistics getTrackerStatistics();

    long getCountOfAllPosts();

    long getTrackerSize();

    HashMap<String, Long> getCountOfAllPeers();
}

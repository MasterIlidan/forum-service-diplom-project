package ru.students.forumservicediplomproject.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
@Setter
public class Statistics {
    private HashMap<String, Long> allTrackerPeers;
    private long allTrackerPosts;
    private double allTrackerSize;
}

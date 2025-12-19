package ru.students.forumservicediplomproject.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ExternalServiceUrls {
    @Value("${openshare.resource.url}")
    private String resourceServiceUrl;
    @Value("openshare.tracker.url")
    private String trackerServiceUrl;
}

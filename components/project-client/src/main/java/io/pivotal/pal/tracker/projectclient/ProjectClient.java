package io.pivotal.pal.tracker.projectclient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;
    private ConcurrentMap<Long,ProjectInfo> cache = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

    @CircuitBreaker(name = "project-client", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo info = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
        cache.put(projectId, info);
        logger.info("Retrieved info for project {}", projectId);
        return info;
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause) throws Throwable {
        logger.info("Getting info for project {} from cache", projectId);
        if (cache.containsKey(projectId)) {
            return cache.get(projectId);
        } else {
            throw cause;
        }
    }
}

package com.vinsguru.netflux.recommendation.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(RestClient userServiceRestClient) {
        this.restClient = userServiceRestClient;
    }

    public List<Long> getWatchlistIds(Long userId) {
        return restClient.get()
                .uri("/api/users/watchlist")
                .header("X-User-Id", String.valueOf(userId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}

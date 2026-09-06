package com.vinsguru.netflux.recommendation.client;

import com.vinsguru.netflux.recommendation.dto.MovieSummary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MovieClient {

    private static final int DEFAULT_LIMIT = 20;

    private final RestClient restClient;

    public MovieClient(RestClient movieServiceRestClient) {
        this.restClient = movieServiceRestClient;
    }

    public List<MovieSummary> getPopular() {
        return restClient.get()
                .uri("/api/movies/popular?limit={limit}", DEFAULT_LIMIT)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<MovieSummary> getLatest() {
        return restClient.get()
                .uri("/api/movies/latest?limit={limit}", DEFAULT_LIMIT)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<MovieSummary> searchByGenre(String genre) {
        return restClient.get()
                .uri("/api/movies/search?genre={genre}&limit={limit}", genre, DEFAULT_LIMIT)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<MovieSummary> getByIds(List<Long> ids) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/movies/batch")
                        .queryParam("ids", ids)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}

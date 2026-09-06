package com.vinsguru.netflux.recommendation.service;

import com.vinsguru.netflux.recommendation.client.MovieClient;
import com.vinsguru.netflux.recommendation.client.UserClient;
import com.vinsguru.netflux.recommendation.dto.HomeResponse;
import com.vinsguru.netflux.recommendation.dto.MovieSummary;
import com.vinsguru.netflux.recommendation.util.Genres;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final MovieClient movieClient;
    private final UserClient userClient;

    public RecommendationService(MovieClient movieClient, UserClient userClient) {
        this.movieClient = movieClient;
        this.userClient = userClient;
    }

    public HomeResponse getHome(Long userId) {
        var watchlist = buildWatchlist(userId);
        var popular = movieClient.getPopular();
        var latest = movieClient.getLatest();
        var byGenre = buildByGenre();
        return new HomeResponse(watchlist, popular, latest, byGenre);
    }

    private List<MovieSummary> buildWatchlist(Long userId) {
        var watchlistIds = userClient.getWatchlistIds(userId);
        if (watchlistIds.isEmpty()) {
            return List.of();
        }
        var moviesById = movieClient.getByIds(watchlistIds).stream()
                .collect(Collectors.toMap(MovieSummary::id, Function.identity()));
        return watchlistIds.stream()
                .map(moviesById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, List<MovieSummary>> buildByGenre() {
        return Genres.ALL.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        movieClient::searchByGenre,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}

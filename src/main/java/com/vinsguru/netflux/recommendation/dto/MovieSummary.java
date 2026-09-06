package com.vinsguru.netflux.recommendation.dto;

import java.time.LocalDate;
import java.util.List;

public record MovieSummary(Long id,
                            String title,
                            String posterPath,
                            String backdropPath,
                            Double voteAverage,
                            LocalDate releaseDate,
                            List<String> genres) {
}

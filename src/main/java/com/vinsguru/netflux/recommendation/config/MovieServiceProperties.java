package com.vinsguru.netflux.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.movie-service")
public record MovieServiceProperties(String baseUrl) {
}

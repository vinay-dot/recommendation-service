package com.vinsguru.netflux.recommendation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient movieServiceRestClient(RestClient.Builder builder, MovieServiceProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }

    @Bean
    public RestClient userServiceRestClient(RestClient.Builder builder, UserServiceProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}

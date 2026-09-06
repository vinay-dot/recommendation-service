package com.vinsguru.netflux.recommendation.controller;

import com.vinsguru.netflux.recommendation.dto.HomeResponse;
import com.vinsguru.netflux.recommendation.util.Genres;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureRestTestClient
class RecommendationApiTest {

    @Autowired
    RestTestClient restTestClient;

    @Autowired
    @Qualifier("movieMockServer")
    MockRestServiceServer movieMockServer;

    @Autowired
    @Qualifier("userMockServer")
    MockRestServiceServer userMockServer;

    @Test
    @DisplayName("Should return home response with watchlist reordered, popular, latest and byGenre")
    void shouldReturnHomeResponseWithWatchlistPopularLatestAndByGenre() {
        userMockServer.expect(requestTo(containsString("/api/users/watchlist")))
                .andExpect(header("X-User-Id", "42"))
                .andRespond(withSuccess("[3,1]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/batch")))
                .andRespond(withSuccess("[" + movieJson(1, "Movie One") + "," + movieJson(3, "Movie Three") + "]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/popular")))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("[" + movieJson(10, "Popular One") + "]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/latest")))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("[" + movieJson(20, "Latest One") + "]", MediaType.APPLICATION_JSON));

        expectAllGenreSearches("Action", "[" + movieJson(30, "Action Movie") + "]");

        var response = restTestClient.get()
                .uri("/api/recommendations/home")
                .header("X-User-Id", "42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(HomeResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.watchlist()).extracting("id").containsExactly(3L, 1L);
        assertThat(response.popular()).extracting("id").containsExactly(10L);
        assertThat(response.latest()).extracting("id").containsExactly(20L);
        assertThat(response.byGenre()).hasSize(Genres.ALL.size());
        assertThat(response.byGenre().get("Action")).extracting("id").containsExactly(30L);
        assertThat(response.byGenre().get("Comedy")).isEmpty();

        movieMockServer.verify();
        userMockServer.verify();
    }

    @Test
    @DisplayName("Should return empty watchlist without calling batch when watchlist is empty")
    void shouldReturnEmptyWatchlistWithoutCallingBatch() {
        userMockServer.expect(requestTo(containsString("/api/users/watchlist")))
                .andExpect(header("X-User-Id", "42"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/popular")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/latest")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        expectAllGenreSearches(null, null);

        var response = restTestClient.get()
                .uri("/api/recommendations/home")
                .header("X-User-Id", "42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(HomeResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.watchlist()).isEmpty();

        movieMockServer.verify();
        userMockServer.verify();
    }

    @Test
    @DisplayName("Should include a genre with no movies as an empty list in byGenre")
    void shouldReturnEmptyListForGenreWithNoMovies() {
        userMockServer.expect(requestTo(containsString("/api/users/watchlist")))
                .andExpect(header("X-User-Id", "42"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/popular")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        movieMockServer.expect(requestTo(containsString("/api/movies/latest")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        expectAllGenreSearches(null, null);

        var response = restTestClient.get()
                .uri("/api/recommendations/home")
                .header("X-User-Id", "42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(HomeResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.byGenre()).hasSize(Genres.ALL.size());
        assertThat(response.byGenre()).containsKey("Western");
        assertThat(response.byGenre().get("Western")).isEmpty();

        movieMockServer.verify();
        userMockServer.verify();
    }

    @Test
    @DisplayName("Should return 401 ProblemDetail when X-User-Id header is missing")
    void shouldReturn401WhenUserIdHeaderMissing() {
        var problem = restTestClient.get()
                .uri("/api/recommendations/home")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(401);
        assertThat(problem.getDetail()).isEqualTo("X-User-Id header is missing or invalid");
    }

    @Test
    @DisplayName("Should return 401 ProblemDetail when X-User-Id header is not numeric")
    void shouldReturn401WhenUserIdHeaderIsNotNumeric() {
        var problem = restTestClient.get()
                .uri("/api/recommendations/home")
                .header("X-User-Id", "abc")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(401);
        assertThat(problem.getDetail()).isEqualTo("X-User-Id header is missing or invalid");
    }

    private void expectAllGenreSearches(String matchGenre, String matchGenreResponseJson) {
        for (String genre : Genres.ALL) {
            var responseJson = genre.equals(matchGenre) ? matchGenreResponseJson : "[]";
            movieMockServer.expect(requestTo(containsString("/api/movies/search")))
                    .andExpect(queryParam("limit", "20"))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
        }
    }

    private String movieJson(long id, String title) {
        return """
                {"id": %d, "title": "%s", "posterPath": "/p.jpg", "backdropPath": "/b.jpg", "voteAverage": 7.5, "releaseDate": "2024-01-01", "genres": ["Action"]}
                """.formatted(id, title).strip();
    }

    @TestConfiguration
    static class MockRestClientTestConfig {

        @Bean
        public RestClient.Builder movieMockRestClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        public MockRestServiceServer movieMockServer(@Qualifier("movieMockRestClientBuilder") RestClient.Builder builder) {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean
        public RestClient movieServiceRestClient(@Qualifier("movieMockRestClientBuilder") RestClient.Builder builder,
                                                  @Qualifier("movieMockServer") MockRestServiceServer movieMockServer) {
            return builder.build();
        }

        @Bean
        public RestClient.Builder userMockRestClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        public MockRestServiceServer userMockServer(@Qualifier("userMockRestClientBuilder") RestClient.Builder builder) {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean
        public RestClient userServiceRestClient(@Qualifier("userMockRestClientBuilder") RestClient.Builder builder,
                                                 @Qualifier("userMockServer") MockRestServiceServer userMockServer) {
            return builder.build();
        }
    }
}

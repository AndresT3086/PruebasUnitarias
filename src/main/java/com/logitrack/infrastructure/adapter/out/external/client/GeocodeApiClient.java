package com.logitrack.infrastructure.adapter.out.external.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GeocodeApiClient {


    private final WebClient webClient;
    private final String apiKey;
    private final int maxRetries;
    private final Duration timeout;

    public GeocodeApiClient(
            WebClient.Builder webClientBuilder,
            @Value("${external.geocode.api.url}") String apiUrl,
            @Value("${external.geocode.api.key}") String apiKey,
            @Value("${external.geocode.api.timeout:5000}") int timeoutMs,
            @Value("${external.geocode.api.retry-attempts:3}") int maxRetries) {

        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .build();
        this.apiKey = apiKey;
        this.maxRetries = maxRetries;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    public Optional<GeocodeResponse> geocodeLocation(String city, String country) {
        String query = String.format("%s, %s", city, country);

        try {
            log.debug("Geocoding location: {}", query);

            GeocodeResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/json")
                            .queryParam("q", query)
                            .queryParam("key", apiKey)
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(GeocodeResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(throwable -> shouldRetry(throwable))
                            .doBeforeRetry(retrySignal ->
                                    log.warn("Retrying geocode request, attempt: {}",
                                            retrySignal.totalRetries() + 1)))
                    .block();

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                log.info("Successfully geocoded location: {}", query);
                return Optional.of(response);
            }

            log.warn("No results found for location: {}", query);
            return Optional.empty();

        } catch (WebClientResponseException e) {
            log.error("HTTP error geocoding location {}: {} - {}",
                    query, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error geocoding location: {}", query, e);
            return Optional.empty();
        }
    }

    public Optional<GeocodeResponse> reverseGeocode(double latitude, double longitude) {
        String coordinates = String.format("%f,%f", latitude, longitude);

        try {
            log.debug("Reverse geocoding coordinates: {}", coordinates);

            GeocodeResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/json")
                            .queryParam("q", coordinates)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GeocodeResponse.class)
                    .timeout(timeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(this::shouldRetry))
                    .block();

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                log.info("Successfully reverse geocoded coordinates: {}", coordinates);
                return Optional.of(response);
            }

            log.warn("No results found for coordinates: {}", coordinates);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error reverse geocoding coordinates: {}", coordinates, e);
            return Optional.empty();
        }
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException e = (WebClientResponseException) throwable;
            // Retry on 5xx errors and specific 4xx errors
            return e.getStatusCode().is5xxServerError() ||
                    e.getStatusCode().value() == 429 || // Too Many Requests
                    e.getStatusCode().value() == 408;   // Request Timeout
        }
        // Retry on timeout and connection errors
        return throwable instanceof java.util.concurrent.TimeoutException ||
                throwable instanceof java.net.ConnectException;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocodeResponse {
        private List<Result> results;
        private Status status;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Result {
            private Components components;
            private Geometry geometry;
            private String formatted;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Components {
            private String city;
            private String town;
            private String village;
            private String country;
            private String countryCode;
            private String state;
            private String postcode;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Geometry {
            private double lat;
            private double lng;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Status {
            private int code;
            private String message;
        }
    }

}

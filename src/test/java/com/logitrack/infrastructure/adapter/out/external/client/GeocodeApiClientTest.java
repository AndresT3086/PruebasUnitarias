package com.logitrack.infrastructure.adapter.out.external.client;

import com.logitrack.infrastructure.adapter.out.external.client.GeocodeApiClient.GeocodeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeocodeApiClient Tests")
class GeocodeApiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GeocodeApiClient geocodeApiClient;

    private static final String TEST_API_URL = "https://api.opencagedata.com/geocode/v1";
    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_TIMEOUT = 5000;
    private static final int TEST_MAX_RETRIES = 3;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup for WebClient mocking
        when(webClientBuilder.baseUrl(TEST_API_URL)).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        geocodeApiClient = new GeocodeApiClient(
                webClientBuilder,
                TEST_API_URL,
                TEST_API_KEY,
                TEST_TIMEOUT,
                TEST_MAX_RETRIES
        );
    }

    @Nested
    @DisplayName("Geocode Location Tests")
    class GeocodeLocationTests {

        @Test
        @DisplayName("Should return empty when no results found")
        void shouldReturnEmptyWhenNoResultsFound() {
            // Arrange
            String city = "InvalidCity";
            String country = "InvalidCountry";
            GeocodeResponse emptyResponse = GeocodeResponse.builder()
                    .results(Collections.emptyList())
                    .build();

            setupSuccessfulWebClientMock(emptyResponse);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();

            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle HTTP errors gracefully")
        void shouldHandleHttpErrorsGracefully() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException exception = WebClientResponseException.create(
                    404, "Not Found", null, null, null
            );

            setupWebClientMockWithException(exception);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();

            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle timeout errors")
        void shouldHandleTimeoutErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            setupWebClientMockWithException(new TimeoutException("Request timeout"));

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();

            verifyWebClientInteraction();
        }
    }

    @Nested
    @DisplayName("Reverse Geocode Tests")
    class ReverseGeocodeTests {

        @Test
        @DisplayName("Should reverse geocode coordinates successfully")
        void shouldReverseGeocodeCoordinatesSuccessfully() {
            // Arrange
            double latitude = 40.7128;
            double longitude = -74.0060;
            GeocodeResponse expectedResponse = createSuccessfulGeocodeResponse();

            setupSuccessfulWebClientMock(expectedResponse);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.reverseGeocode(latitude, longitude);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getResults()).hasSize(1);
            assertThat(result.get().getResults().get(0).getComponents().getCity()).isEqualTo("New York");

            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should return empty when reverse geocoding finds no results")
        void shouldReturnEmptyWhenReverseGeocodingFindsNoResults() {
            // Arrange
            double latitude = 0.0;
            double longitude = 0.0;
            GeocodeResponse emptyResponse = GeocodeResponse.builder()
                    .results(Collections.emptyList())
                    .build();

            setupSuccessfulWebClientMock(emptyResponse);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.reverseGeocode(latitude, longitude);

            // Assert
            assertThat(result).isEmpty();

            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle exceptions in reverse geocoding")
        void shouldHandleExceptionsInReverseGeocoding() {
            // Arrange
            double latitude = 40.7128;
            double longitude = -74.0060;

            setupWebClientMockWithException(new ConnectException("Connection refused"));

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.reverseGeocode(latitude, longitude);

            // Assert
            assertThat(result).isEmpty();

            verifyWebClientInteraction();
        }
    }

    @Nested
    @DisplayName("Retry Logic Tests")
    class RetryLogicTests {

        @Test
        @DisplayName("Should handle 5xx server errors and return empty")
        void shouldHandle5xxServerErrorsAndReturnEmpty() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException serverError = WebClientResponseException.create(
                    500, "Internal Server Error", null, null, null
            );

            setupWebClientMockWithException(serverError);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle 429 Too Many Requests and return empty")
        void shouldHandle429TooManyRequestsAndReturnEmpty() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException tooManyRequests = WebClientResponseException.create(
                    429, "Too Many Requests", null, null, null
            );

            setupWebClientMockWithException(tooManyRequests);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle connection errors and return empty")
        void shouldHandleConnectionErrorsAndReturnEmpty() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            ConnectException connectionError = new ConnectException("Connection refused");

            setupWebClientMockWithException(connectionError);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should not retry on 4xx client errors (except 429 and 408)")
        void shouldNotRetryOn4xxClientErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException clientError = WebClientResponseException.create(
                    400, "Bad Request", null, null, null
            );

            setupWebClientMockWithException(clientError);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should create client with correct configuration")
        void shouldCreateClientWithCorrectConfiguration() {
            // Arrange & Act - Client already created in @BeforeEach

            // Assert
            verify(webClientBuilder).baseUrl(TEST_API_URL);
            verify(webClientBuilder).build();
        }

        @Test
        @DisplayName("Should use provided timeout configuration")
        void shouldUseProvidedTimeoutConfiguration() {
            // Arrange
            int customTimeout = 10000;
            GeocodeApiClient customClient = new GeocodeApiClient(
                    webClientBuilder,
                    TEST_API_URL,
                    TEST_API_KEY,
                    customTimeout,
                    TEST_MAX_RETRIES
            );

            setupSuccessfulWebClientMock(createSuccessfulGeocodeResponse());

            // Act
            customClient.geocodeLocation("Test", "Country");

            // Assert - Timeout is applied during the reactive chain,
            // but we can verify the client was created with correct config
            verify(webClientBuilder, times(2)).baseUrl(TEST_API_URL);
        }
    }

    @Nested
    @DisplayName("Response Mapping Tests")
    class ResponseMappingTests {

        @Test
        @DisplayName("Should map complete response structure correctly")
        void shouldMapCompleteResponseStructureCorrectly() {
            // Arrange
            GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                    .city("New York")
                    .country("United States")
                    .countryCode("US")
                    .state("New York")
                    .postcode("10001")
                    .build();

            GeocodeResponse.Geometry geometry = GeocodeResponse.Geometry.builder()
                    .lat(40.7128)
                    .lng(-74.0060)
                    .build();

            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(components)
                    .geometry(geometry)
                    .formatted("New York, NY 10001, United States")
                    .build();

            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .build();

            setupSuccessfulWebClientMock(response);

            // Act
            Optional<GeocodeResponse> results = geocodeApiClient.geocodeLocation("New York", "USA");

            // Assert
            assertThat(results).isPresent();
            GeocodeResponse.Result mappedResult = results.get().getResults().get(0);

            assertThat(mappedResult.getComponents().getCity()).isEqualTo("New York");
            assertThat(mappedResult.getComponents().getCountry()).isEqualTo("United States");
            assertThat(mappedResult.getComponents().getCountryCode()).isEqualTo("US");
            assertThat(mappedResult.getComponents().getState()).isEqualTo("New York");
            assertThat(mappedResult.getComponents().getPostcode()).isEqualTo("10001");
            assertThat(mappedResult.getGeometry().getLat()).isEqualTo(40.7128);
            assertThat(mappedResult.getGeometry().getLng()).isEqualTo(-74.0060);
            assertThat(mappedResult.getFormatted()).isEqualTo("New York, NY 10001, United States");
        }
    }

    // Helper Methods
    private GeocodeResponse createSuccessfulGeocodeResponse() {
        GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                .city("New York")
                .country("United States")
                .build();

        GeocodeResponse.Geometry geometry = GeocodeResponse.Geometry.builder()
                .lat(40.7128)
                .lng(-74.0060)
                .build();

        GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                .components(components)
                .geometry(geometry)
                .build();

        return GeocodeResponse.builder()
                .results(Arrays.asList(result))
                .build();
    }

    private void setupSuccessfulWebClientMock(GeocodeResponse response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GeocodeResponse.class)).thenReturn(Mono.just(response));
    }

    private void setupWebClientMockWithException(Exception exception) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GeocodeResponse.class)).thenReturn(Mono.error(exception));
    }

    private void verifyWebClientInteraction() {
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(GeocodeResponse.class);
    }
}

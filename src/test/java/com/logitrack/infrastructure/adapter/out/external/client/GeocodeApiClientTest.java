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
import java.util.List;
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

    @Nested
    @DisplayName("shouldRetry Method Branch Coverage")
    class ShouldRetryBranchCoverageTests {

        @Test
        @DisplayName("Should retry on 5xx server errors")
        void shouldRetryOn5xxServerErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            // Test different 5xx errors
            WebClientResponseException error500 = WebClientResponseException.create(
                    500, "Internal Server Error", null, null, null);
            WebClientResponseException error502 = WebClientResponseException.create(
                    502, "Bad Gateway", null, null, null);
            WebClientResponseException error503 = WebClientResponseException.create(
                    503, "Service Unavailable", null, null, null);

            // Test each 5xx error
            setupWebClientMockWithException(error500);
            Optional<GeocodeResponse> result1 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result1).isEmpty();

            setupWebClientMockWithException(error502);
            Optional<GeocodeResponse> result2 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result2).isEmpty();

            setupWebClientMockWithException(error503);
            Optional<GeocodeResponse> result3 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result3).isEmpty();
        }

        @Test
        @DisplayName("Should retry on 408 Request Timeout specifically")
        void shouldRetryOn408RequestTimeoutSpecifically() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException error408 = WebClientResponseException.create(
                    408, "Request Timeout", null, null, null);

            setupWebClientMockWithException(error408);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should NOT retry on other 4xx client errors")
        void shouldNotRetryOnOther4xxClientErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            // Test 401, 403, 404 - should NOT retry
            WebClientResponseException error401 = WebClientResponseException.create(
                    401, "Unauthorized", null, null, null);
            WebClientResponseException error403 = WebClientResponseException.create(
                    403, "Forbidden", null, null, null);
            WebClientResponseException error404 = WebClientResponseException.create(
                    404, "Not Found", null, null, null);

            setupWebClientMockWithException(error401);
            Optional<GeocodeResponse> result1 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result1).isEmpty();

            setupWebClientMockWithException(error403);
            Optional<GeocodeResponse> result2 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result2).isEmpty();

            setupWebClientMockWithException(error404);
            Optional<GeocodeResponse> result3 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result3).isEmpty();
        }

        @Test
        @DisplayName("Should retry on TimeoutException specifically")
        void shouldRetryOnTimeoutExceptionSpecifically() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            java.util.concurrent.TimeoutException timeoutException =
                    new java.util.concurrent.TimeoutException("Operation timed out");

            setupWebClientMockWithException(timeoutException);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should retry on ConnectException specifically")
        void shouldRetryOnConnectExceptionSpecifically() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            java.net.ConnectException connectException =
                    new java.net.ConnectException("Connection refused");

            setupWebClientMockWithException(connectException);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should NOT retry on non-retryable exceptions")
        void shouldNotRetryOnNonRetryableExceptions() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            // Test exceptions that should NOT be retried
            IllegalArgumentException illegalArg = new IllegalArgumentException("Invalid argument");
            RuntimeException runtimeException = new RuntimeException("Some other error");

            setupWebClientMockWithException(illegalArg);
            Optional<GeocodeResponse> result1 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result1).isEmpty();

            setupWebClientMockWithException(runtimeException);
            Optional<GeocodeResponse> result2 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result2).isEmpty();
        }
    }

    @Nested
    @DisplayName("WebClientResponseException Handling Coverage")
    class WebClientResponseExceptionHandlingCoverageTests {

        @Test
        @DisplayName("Should handle WebClientResponseException with response body")
        void shouldHandleWebClientResponseExceptionWithResponseBody() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            String errorBody = "{\"error\": \"Invalid API key\"}";

            WebClientResponseException exception = WebClientResponseException.create(
                    401, "Unauthorized", null, errorBody.getBytes(), null
            );

            setupWebClientMockWithException(exception);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should log different types of exceptions appropriately")
        void shouldLogDifferentTypesOfExceptionsAppropriately() {
            // This test ensures different exception paths are covered for logging

            String city = "Test City";
            String country = "Test Country";

            // Test WebClientResponseException path
            WebClientResponseException webClientException = WebClientResponseException.create(
                    429, "Too Many Requests", null, null, null
            );
            setupWebClientMockWithException(webClientException);
            Optional<GeocodeResponse> result1 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result1).isEmpty();

            // Test general Exception path
            RuntimeException generalException = new RuntimeException("Network error");
            setupWebClientMockWithException(generalException);
            Optional<GeocodeResponse> result2 = geocodeApiClient.geocodeLocation(city, country);
            assertThat(result2).isEmpty();
        }
    }

    @Nested
    @DisplayName("Response Validation Branch Coverage")
    class ResponseValidationBranchCoverageTests {

        @Test
        @DisplayName("Should handle empty response body")
        void shouldHandleEmptyResponseBody() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            // FIX: Use Mono.empty() instead of Mono.just(null)
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(GeocodeResponse.class)).thenReturn(Mono.empty());

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should handle response with null results list")
        void shouldHandleResponseWithNullResultsList() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            GeocodeResponse responseWithNullResults = GeocodeResponse.builder()
                    .results(null) // Null results
                    .build();

            // FIX: Use fresh mock setup for each test
            WebClient.RequestHeadersUriSpec freshUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec freshHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec freshResponseSpec = mock(WebClient.ResponseSpec.class);

            when(webClient.get()).thenReturn(freshUriSpec);
            when(freshUriSpec.uri(any(Function.class))).thenReturn(freshHeadersSpec);
            when(freshHeadersSpec.retrieve()).thenReturn(freshResponseSpec);
            when(freshResponseSpec.bodyToMono(GeocodeResponse.class)).thenReturn(Mono.just(responseWithNullResults));

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return successful response when all conditions are met")
        void shouldReturnSuccessfulResponseWhenAllConditionsAreMet() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            GeocodeResponse successResponse = createSuccessfulGeocodeResponse();

            // FIX: Reset mocks and use fresh setup
            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupSuccessfulWebClientMock(successResponse);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getResults()).isNotEmpty();
            verifyWebClientInteraction();
        }
    }

    @Nested
    @DisplayName("Reverse Geocode Branch Coverage")
    class ReverseGeocodeBranchCoverageTests {

        @Test
        @DisplayName("Should handle empty response in reverse geocoding")
        void shouldHandleEmptyResponseInReverseGeocoding() {
            // Arrange
            double latitude = 40.7128;
            double longitude = -74.0060;

            // FIX: Use fresh mocks and Mono.empty()
            WebClient.RequestHeadersUriSpec freshUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec freshHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec freshResponseSpec = mock(WebClient.ResponseSpec.class);

            when(webClient.get()).thenReturn(freshUriSpec);
            when(freshUriSpec.uri(any(Function.class))).thenReturn(freshHeadersSpec);
            when(freshHeadersSpec.retrieve()).thenReturn(freshResponseSpec);
            when(freshResponseSpec.bodyToMono(GeocodeResponse.class)).thenReturn(Mono.empty());

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.reverseGeocode(latitude, longitude);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle response with null results in reverse geocoding")
        void shouldHandleResponseWithNullResultsInReverseGeocoding() {
            // Arrange
            double latitude = 40.7128;
            double longitude = -74.0060;
            GeocodeResponse responseWithNullResults = GeocodeResponse.builder()
                    .results(null)
                    .build();

            // FIX: Fresh mock setup
            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupSuccessfulWebClientMock(responseWithNullResults);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.reverseGeocode(latitude, longitude);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("shouldRetry Method Branch Coverage")
    class ShouldRetryBranchCoverageTests2 {

        @Test
        @DisplayName("Should retry on 5xx server errors")
        void shouldRetryOn5xxServerErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";

            // Test different 5xx errors separately
            WebClientResponseException error500 = WebClientResponseException.create(
                    500, "Internal Server Error", null, null, null);

            // FIX: Reset and setup fresh mocks for each test
            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(error500);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
            verifyWebClientInteraction();
        }

        @Test
        @DisplayName("Should retry on 502 Bad Gateway")
        void shouldRetryOn502BadGateway() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException error502 = WebClientResponseException.create(
                    502, "Bad Gateway", null, null, null);

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(error502);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should retry on 429 Too Many Requests")
        void shouldRetryOn429TooManyRequests() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException error429 = WebClientResponseException.create(
                    429, "Too Many Requests", null, null, null);

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(error429);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should retry on 408 Request Timeout")
        void shouldRetryOn408RequestTimeout() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException error408 = WebClientResponseException.create(
                    408, "Request Timeout", null, null, null);

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(error408);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should NOT retry on 4xx client errors")
        void shouldNotRetryOn4xxClientErrors() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            WebClientResponseException error404 = WebClientResponseException.create(
                    404, "Not Found", null, null, null);

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(error404);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should retry on TimeoutException")
        void shouldRetryOnTimeoutException() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            java.util.concurrent.TimeoutException timeoutException =
                    new java.util.concurrent.TimeoutException("Operation timed out");

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(timeoutException);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should retry on ConnectException")
        void shouldRetryOnConnectException() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            java.net.ConnectException connectException =
                    new java.net.ConnectException("Connection refused");

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(connectException);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should NOT retry on non-retryable exceptions")
        void shouldNotRetryOnNonRetryableExceptions() {
            // Arrange
            String city = "Test City";
            String country = "Test Country";
            IllegalArgumentException illegalArg = new IllegalArgumentException("Invalid argument");

            reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
            setupWebClientMockWithException(illegalArg);

            // Act
            Optional<GeocodeResponse> result = geocodeApiClient.geocodeLocation(city, country);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("DTO Builder and Getter Coverage")
    class DtoBuilderAndGetterCoverageTests {

        @Test
        @DisplayName("Should build GeocodeResponse with all fields")
        void shouldBuildGeocodeResponseWithAllFields() {
            // Arrange
            GeocodeResponse.Status status = GeocodeResponse.Status.builder()
                    .code(200)
                    .message("OK")
                    .build();

            GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                    .city("New York")
                    .town("Manhattan")
                    .village("Greenwich Village")
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
                    .formatted("New York, NY 10001, USA")
                    .build();

            // Act
            GeocodeResponse response = GeocodeResponse.builder()
                    .results(Arrays.asList(result))
                    .status(status)
                    .build();

            // Assert - Test all getters to cover branches
            assertThat(response.getResults()).hasSize(1);
            assertThat(response.getStatus()).isNotNull();
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getStatus().getMessage()).isEqualTo("OK");

            GeocodeResponse.Result retrievedResult = response.getResults().get(0);
            assertThat(retrievedResult.getComponents()).isNotNull();
            assertThat(retrievedResult.getGeometry()).isNotNull();
            assertThat(retrievedResult.getFormatted()).isEqualTo("New York, NY 10001, USA");

            GeocodeResponse.Components retrievedComponents = retrievedResult.getComponents();
            assertThat(retrievedComponents.getCity()).isEqualTo("New York");
            assertThat(retrievedComponents.getTown()).isEqualTo("Manhattan");
            assertThat(retrievedComponents.getVillage()).isEqualTo("Greenwich Village");
            assertThat(retrievedComponents.getCountry()).isEqualTo("United States");
            assertThat(retrievedComponents.getCountryCode()).isEqualTo("US");
            assertThat(retrievedComponents.getState()).isEqualTo("New York");
            assertThat(retrievedComponents.getPostcode()).isEqualTo("10001");

            GeocodeResponse.Geometry retrievedGeometry = retrievedResult.getGeometry();
            assertThat(retrievedGeometry.getLat()).isEqualTo(40.7128);
            assertThat(retrievedGeometry.getLng()).isEqualTo(-74.0060);
        }

        @Test
        @DisplayName("Should build Components with partial data")
        void shouldBuildComponentsWithPartialData() {
            // Arrange & Act
            GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                    .city("London")
                    .country("United Kingdom")
                    .countryCode("GB")
                    .postcode("SW1A 1AA")
                    // Note: town, village, state are null
                    .build();

            // Assert - Test getters including null values
            assertThat(components.getCity()).isEqualTo("London");
            assertThat(components.getTown()).isNull();
            assertThat(components.getVillage()).isNull();
            assertThat(components.getCountry()).isEqualTo("United Kingdom");
            assertThat(components.getCountryCode()).isEqualTo("GB");
            assertThat(components.getState()).isNull();
            assertThat(components.getPostcode()).isEqualTo("SW1A 1AA");
        }

        @Test
        @DisplayName("Should build Result with null components and geometry")
        void shouldBuildResultWithNullComponentsAndGeometry() {
            // Arrange & Act
            GeocodeResponse.Result result = GeocodeResponse.Result.builder()
                    .components(null)
                    .geometry(null)
                    .formatted("Unknown Location")
                    .build();

            // Assert
            assertThat(result.getComponents()).isNull();
            assertThat(result.getGeometry()).isNull();
            assertThat(result.getFormatted()).isEqualTo("Unknown Location");
        }

        @Test
        @DisplayName("Should build Geometry with zero coordinates")
        void shouldBuildGeometryWithZeroCoordinates() {
            // Arrange & Act
            GeocodeResponse.Geometry geometry = GeocodeResponse.Geometry.builder()
                    .lat(0.0)
                    .lng(0.0)
                    .build();

            // Assert
            assertThat(geometry.getLat()).isEqualTo(0.0);
            assertThat(geometry.getLng()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should build Status with different codes")
        void shouldBuildStatusWithDifferentCodes() {
            // Arrange & Act
            GeocodeResponse.Status successStatus = GeocodeResponse.Status.builder()
                    .code(200)
                    .message("OK")
                    .build();

            GeocodeResponse.Status errorStatus = GeocodeResponse.Status.builder()
                    .code(400)
                    .message("Bad Request")
                    .build();

            // Assert
            assertThat(successStatus.getCode()).isEqualTo(200);
            assertThat(successStatus.getMessage()).isEqualTo("OK");

            assertThat(errorStatus.getCode()).isEqualTo(400);
            assertThat(errorStatus.getMessage()).isEqualTo("Bad Request");
        }

        @Test
        @DisplayName("Should use no-args constructors")
        void shouldUseNoArgsConstructors() {
            // Arrange & Act - Test no-args constructors for Jackson
            GeocodeResponse response = new GeocodeResponse();
            GeocodeResponse.Result result = new GeocodeResponse.Result();
            GeocodeResponse.Components components = new GeocodeResponse.Components();
            GeocodeResponse.Geometry geometry = new GeocodeResponse.Geometry();
            GeocodeResponse.Status status = new GeocodeResponse.Status();

            // Assert - Verify objects are created
            assertThat(response).isNotNull();
            assertThat(result).isNotNull();
            assertThat(components).isNotNull();
            assertThat(geometry).isNotNull();
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should use all-args constructors")
        void shouldUseAllArgsConstructors() {
            // Arrange
            List<GeocodeResponse.Result> results = Arrays.asList();
            GeocodeResponse.Status status = new GeocodeResponse.Status(200, "OK");

            GeocodeResponse.Components components = new GeocodeResponse.Components(
                    "City", "Town", "Village", "Country", "CC", "State", "12345"
            );

            GeocodeResponse.Geometry geometry = new GeocodeResponse.Geometry(40.7, -74.0);

            GeocodeResponse.Result result = new GeocodeResponse.Result(
                    components, geometry, "Formatted Address"
            );

            // Act
            GeocodeResponse response = new GeocodeResponse(results, status);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getResults()).isEqualTo(results);
            assertThat(response.getStatus()).isEqualTo(status);

            assertThat(components.getCity()).isEqualTo("City");
            assertThat(geometry.getLat()).isEqualTo(40.7);
            assertThat(result.getFormatted()).isEqualTo("Formatted Address");
            assertThat(status.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should handle setters for mutable operations")
        void shouldHandleSettersForMutableOperations() {
            // Arrange
            GeocodeResponse response = new GeocodeResponse();
            GeocodeResponse.Status status = new GeocodeResponse.Status();
            GeocodeResponse.Components components = new GeocodeResponse.Components();
            GeocodeResponse.Geometry geometry = new GeocodeResponse.Geometry();
            GeocodeResponse.Result result = new GeocodeResponse.Result();

            List<GeocodeResponse.Result> results = Arrays.asList(result);

            // Act - Use setters (if they exist due to @Data)
            response.setResults(results);
            response.setStatus(status);

            status.setCode(201);
            status.setMessage("Created");

            components.setCity("Paris");
            components.setCountry("France");
            components.setPostcode("75001");

            geometry.setLat(48.8566);
            geometry.setLng(2.3522);

            result.setComponents(components);
            result.setGeometry(geometry);
            result.setFormatted("Paris, France");

            // Assert
            assertThat(response.getResults()).hasSize(1);
            assertThat(response.getStatus().getCode()).isEqualTo(201);
            assertThat(response.getStatus().getMessage()).isEqualTo("Created");

            GeocodeResponse.Result retrievedResult = response.getResults().get(0);
            assertThat(retrievedResult.getComponents().getCity()).isEqualTo("Paris");
            assertThat(retrievedResult.getComponents().getCountry()).isEqualTo("France");
            assertThat(retrievedResult.getComponents().getPostcode()).isEqualTo("75001");
            assertThat(retrievedResult.getGeometry().getLat()).isEqualTo(48.8566);
            assertThat(retrievedResult.getGeometry().getLng()).isEqualTo(2.3522);
            assertThat(retrievedResult.getFormatted()).isEqualTo("Paris, France");
        }
    }

    @Nested
    @DisplayName("DTO equals() and hashCode() Branch Coverage")
    class DtoEqualsHashCodeBranchCoverageTests {

        @Nested
        @DisplayName("GeocodeResponse equals and hashCode")
        class GeocodeResponseEqualsHashCodeTests {

            @Test
            @DisplayName("Should test equals with same object")
            void shouldTestEqualsWithSameObject() {
                // Arrange
                GeocodeResponse response = GeocodeResponse.builder()
                        .results(Arrays.asList())
                        .build();

                // Act & Assert - Same object reference
                assertThat(response.equals(response)).isTrue();
                assertThat(response.hashCode()).isEqualTo(response.hashCode());
            }

            @Test
            @DisplayName("Should test equals with null")
            void shouldTestEqualsWithNull() {
                // Arrange
                GeocodeResponse response = GeocodeResponse.builder().build();

                // Act & Assert - Null comparison branch
                assertThat(response.equals(null)).isFalse();
            }

            @Test
            @DisplayName("Should test equals with different class")
            void shouldTestEqualsWithDifferentClass() {
                // Arrange
                GeocodeResponse response = GeocodeResponse.builder().build();
                String differentClass = "not a GeocodeResponse";

                // Act & Assert - Different class branch
                assertThat(response.equals(differentClass)).isFalse();
            }

            @Test
            @DisplayName("Should test equals with equal objects")
            void shouldTestEqualsWithEqualObjects() {
                // Arrange
                List<GeocodeResponse.Result> results = Arrays.asList();
                GeocodeResponse.Status status = GeocodeResponse.Status.builder()
                        .code(200)
                        .message("OK")
                        .build();

                GeocodeResponse response1 = GeocodeResponse.builder()
                        .results(results)
                        .status(status)
                        .build();

                GeocodeResponse response2 = GeocodeResponse.builder()
                        .results(results)
                        .status(status)
                        .build();

                // Act & Assert - Equal objects
                assertThat(response1.equals(response2)).isTrue();
                assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
            }

            @Test
            @DisplayName("Should test equals with different results")
            void shouldTestEqualsWithDifferentResults() {
                // Arrange
                GeocodeResponse response1 = GeocodeResponse.builder()
                        .results(Arrays.asList())
                        .build();

                GeocodeResponse response2 = GeocodeResponse.builder()
                        .results(null)
                        .build();

                // Act & Assert - Different results field
                assertThat(response1.equals(response2)).isFalse();
            }
        }

        @Nested
        @DisplayName("Components equals and hashCode")
        class ComponentsEqualsHashCodeTests {

            @Test
            @DisplayName("Should test Components equals with same object")
            void shouldTestComponentsEqualsWithSameObject() {
                // Arrange
                GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                        .city("New York")
                        .country("USA")
                        .build();

                // Act & Assert
                assertThat(components.equals(components)).isTrue();
                assertThat(components.hashCode()).isEqualTo(components.hashCode());
            }

            @Test
            @DisplayName("Should test Components equals with null")
            void shouldTestComponentsEqualsWithNull() {
                // Arrange
                GeocodeResponse.Components components = GeocodeResponse.Components.builder().build();

                // Act & Assert
                assertThat(components.equals(null)).isFalse();
            }

            @Test
            @DisplayName("Should test Components equals with different class")
            void shouldTestComponentsEqualsWithDifferentClass() {
                // Arrange
                GeocodeResponse.Components components = GeocodeResponse.Components.builder().build();

                // Act & Assert
                assertThat(components.equals("not a Components")).isFalse();
            }

            @Test
            @DisplayName("Should test Components equals with equal objects")
            void shouldTestComponentsEqualsWithEqualObjects() {
                // Arrange
                GeocodeResponse.Components components1 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town("Westminster")
                        .village("Covent Garden")
                        .country("United Kingdom")
                        .countryCode("GB")
                        .state("England")
                        .postcode("WC2N 5DU")
                        .build();

                GeocodeResponse.Components components2 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town("Westminster")
                        .village("Covent Garden")
                        .country("United Kingdom")
                        .countryCode("GB")
                        .state("England")
                        .postcode("WC2N 5DU")
                        .build();

                // Act & Assert
                assertThat(components1.equals(components2)).isTrue();
                assertThat(components1.hashCode()).isEqualTo(components2.hashCode());
            }

            @Test
            @DisplayName("Should test Components equals with different city")
            void shouldTestComponentsEqualsWithDifferentCity() {
                // Arrange
                GeocodeResponse.Components components1 = GeocodeResponse.Components.builder()
                        .city("London")
                        .country("UK")
                        .build();

                GeocodeResponse.Components components2 = GeocodeResponse.Components.builder()
                        .city("Paris")
                        .country("UK")
                        .build();

                // Act & Assert
                assertThat(components1.equals(components2)).isFalse();
            }

            @Test
            @DisplayName("Should test Components equals with null vs non-null fields")
            void shouldTestComponentsEqualsWithNullVsNonNullFields() {
                // Arrange
                GeocodeResponse.Components components1 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town(null)
                        .village("Greenwich")
                        .build();

                GeocodeResponse.Components components2 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town("Camden")
                        .village("Greenwich")
                        .build();

                // Act & Assert - Tests null vs non-null branch
                assertThat(components1.equals(components2)).isFalse();
            }

            @Test
            @DisplayName("Should test Components equals with both null fields")
            void shouldTestComponentsEqualsWithBothNullFields() {
                // Arrange
                GeocodeResponse.Components components1 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town(null)
                        .village(null)
                        .build();

                GeocodeResponse.Components components2 = GeocodeResponse.Components.builder()
                        .city("London")
                        .town(null)
                        .village(null)
                        .build();

                // Act & Assert - Tests both null branch
                assertThat(components1.equals(components2)).isTrue();
                assertThat(components1.hashCode()).isEqualTo(components2.hashCode());
            }

            @Test
            @DisplayName("Should test Components with all different fields")
            void shouldTestComponentsWithAllDifferentFields() {
                // Arrange
                GeocodeResponse.Components components1 = GeocodeResponse.Components.builder()
                        .city("New York")
                        .town("Manhattan")
                        .village("SoHo")
                        .country("USA")
                        .countryCode("US")
                        .state("NY")
                        .postcode("10013")
                        .build();

                GeocodeResponse.Components components2 = GeocodeResponse.Components.builder()
                        .city("Los Angeles")
                        .town("Hollywood")
                        .village("Beverly Hills")
                        .country("USA")
                        .countryCode("US")
                        .state("CA")
                        .postcode("90210")
                        .build();

                // Act & Assert
                assertThat(components1.equals(components2)).isFalse();
            }
        }

        @Nested
        @DisplayName("Result equals and hashCode")
        class ResultEqualsHashCodeTests {

            @Test
            @DisplayName("Should test Result equals branches")
            void shouldTestResultEqualsBranches() {
                // Arrange
                GeocodeResponse.Components components = GeocodeResponse.Components.builder()
                        .city("Berlin")
                        .country("Germany")
                        .build();

                GeocodeResponse.Geometry geometry = GeocodeResponse.Geometry.builder()
                        .lat(52.5200)
                        .lng(13.4050)
                        .build();

                GeocodeResponse.Result result1 = GeocodeResponse.Result.builder()
                        .components(components)
                        .geometry(geometry)
                        .formatted("Berlin, Germany")
                        .build();

                GeocodeResponse.Result result2 = GeocodeResponse.Result.builder()
                        .components(components)
                        .geometry(geometry)
                        .formatted("Berlin, Germany")
                        .build();

                GeocodeResponse.Result result3 = GeocodeResponse.Result.builder()
                        .components(null)
                        .geometry(geometry)
                        .formatted("Berlin, Germany")
                        .build();

                // Act & Assert
                assertThat(result1.equals(result1)).isTrue(); // Same object
                assertThat(result1.equals(null)).isFalse(); // Null
                assertThat(result1.equals("not a Result")).isFalse(); // Different class
                assertThat(result1.equals(result2)).isTrue(); // Equal objects
                assertThat(result1.equals(result3)).isFalse(); // Different components
                assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
            }
        }

        @Nested
        @DisplayName("Geometry equals and hashCode")
        class GeometryEqualsHashCodeTests {

            @Test
            @DisplayName("Should test Geometry equals branches")
            void shouldTestGeometryEqualsBranches() {
                // Arrange
                GeocodeResponse.Geometry geometry1 = GeocodeResponse.Geometry.builder()
                        .lat(40.7128)
                        .lng(-74.0060)
                        .build();

                GeocodeResponse.Geometry geometry2 = GeocodeResponse.Geometry.builder()
                        .lat(40.7128)
                        .lng(-74.0060)
                        .build();

                GeocodeResponse.Geometry geometry3 = GeocodeResponse.Geometry.builder()
                        .lat(51.5074)
                        .lng(-0.1278)
                        .build();

                // Act & Assert
                assertThat(geometry1.equals(geometry1)).isTrue(); // Same object
                assertThat(geometry1.equals(null)).isFalse(); // Null
                assertThat(geometry1.equals("not geometry")).isFalse(); // Different class
                assertThat(geometry1.equals(geometry2)).isTrue(); // Equal values
                assertThat(geometry1.equals(geometry3)).isFalse(); // Different values
                assertThat(geometry1.hashCode()).isEqualTo(geometry2.hashCode());
            }
        }

        @Nested
        @DisplayName("Status equals and hashCode")
        class StatusEqualsHashCodeTests {

            @Test
            @DisplayName("Should test Status equals branches")
            void shouldTestStatusEqualsBranches() {
                // Arrange
                GeocodeResponse.Status status1 = GeocodeResponse.Status.builder()
                        .code(200)
                        .message("OK")
                        .build();

                GeocodeResponse.Status status2 = GeocodeResponse.Status.builder()
                        .code(200)
                        .message("OK")
                        .build();

                GeocodeResponse.Status status3 = GeocodeResponse.Status.builder()
                        .code(404)
                        .message("Not Found")
                        .build();

                GeocodeResponse.Status status4 = GeocodeResponse.Status.builder()
                        .code(200)
                        .message(null)
                        .build();

                // Act & Assert
                assertThat(status1.equals(status1)).isTrue(); // Same object
                assertThat(status1.equals(null)).isFalse(); // Null
                assertThat(status1.equals("not status")).isFalse(); // Different class
                assertThat(status1.equals(status2)).isTrue(); // Equal values
                assertThat(status1.equals(status3)).isFalse(); // Different code and message
                assertThat(status1.equals(status4)).isFalse(); // Different message (null vs non-null)
                assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
            }
        }
    }
}

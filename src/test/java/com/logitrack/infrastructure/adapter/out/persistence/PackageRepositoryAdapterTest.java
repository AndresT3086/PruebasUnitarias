package com.logitrack.infrastructure.adapter.out.persistence;
import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.port.out.PackageRepository;
import com.logitrack.infrastructure.adapter.out.persistence.entity.LocationHistoryEntity;
import com.logitrack.infrastructure.adapter.out.persistence.entity.PackageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackageRepositoryAdapter Tests")
class PackageRepositoryAdapterTest {

    @Mock
    private PackageJpaRepository jpaRepository;

    @InjectMocks
    private PackageRepositoryAdapter packageRepositoryAdapter;

    private Package testPackage;
    private PackageEntity testEntity;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        // Arrange - Common test data setup
        testDateTime = LocalDateTime.now();

        // Domain package setup
        Recipient.Address address = Recipient.Address.builder()
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("12345")
                .build();

        Recipient recipient = Recipient.builder()
                .name("John Doe")
                .email("john@test.com")
                .phone("+1234567890")
                .address(address)
                .build();

        testPackage = Package.builder()
                .id(PackageId.of("LT-TEST12345"))
                .recipient(recipient)
                .dimensions(Dimensions.of(20, 15, 10))
                .weight(Weight.ofKilograms(2.5))
                .notes("Test package")
                .build();

        // Entity setup
        testEntity = PackageEntity.builder()
                .id("LT-TEST12345")
                .recipientName("John Doe")
                .recipientEmail("john@test.com")
                .recipientPhone("+1234567890")
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .postalCode("12345")
                .height(BigDecimal.valueOf(20))
                .width(BigDecimal.valueOf(15))
                .depth(BigDecimal.valueOf(10))
                .weight(BigDecimal.valueOf(2.5))
                .status(PackageStatus.CREATED)
                .notes("Test package")
                .deleted(false)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .locationHistory(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Save Package Tests")
    class SavePackageTests {

        @Test
        @DisplayName("Should save package successfully")
        void shouldSavePackageSuccessfully() {
            // Arrange
            when(jpaRepository.save(any(PackageEntity.class))).thenReturn(testEntity);

            // Act
            Package result = packageRepositoryAdapter.save(testPackage);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId().getValue()).isEqualTo("LT-TEST12345");
            assertThat(result.getRecipient().getName()).isEqualTo("John Doe");
            assertThat(result.getWeight().toKilograms()).isEqualTo(2.5);

            verify(jpaRepository).save(any(PackageEntity.class));
        }

        @Test
        @DisplayName("Should map domain package to entity correctly")
        void shouldMapDomainPackageToEntityCorrectly() {
            // Arrange
            ArgumentCaptor<PackageEntity> entityCaptor = ArgumentCaptor.forClass(PackageEntity.class);
            when(jpaRepository.save(any(PackageEntity.class))).thenReturn(testEntity);

            // Act
            packageRepositoryAdapter.save(testPackage);

            // Assert
            verify(jpaRepository).save(entityCaptor.capture());
            PackageEntity savedEntity = entityCaptor.getValue();

            assertThat(savedEntity.getId()).isEqualTo("LT-TEST12345");
            assertThat(savedEntity.getRecipientName()).isEqualTo("John Doe");
            assertThat(savedEntity.getRecipientEmail()).isEqualTo("john@test.com");
            assertThat(savedEntity.getStreet()).isEqualTo("123 Test St");
            assertThat(savedEntity.getWeight()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
            assertThat(savedEntity.getStatus()).isEqualTo(PackageStatus.CREATED);
        }

        @Test
        @DisplayName("Should save package with location history")
        void shouldSavePackageWithLocationHistory() {
            // Arrange
            Location location = Location.create("Origin City", "Origin Country", "Package created", 40.7128, -74.0060);
            testPackage.addLocation(location);

            ArgumentCaptor<PackageEntity> entityCaptor = ArgumentCaptor.forClass(PackageEntity.class);
            when(jpaRepository.save(any(PackageEntity.class))).thenReturn(testEntity);

            // Act
            packageRepositoryAdapter.save(testPackage);

            // Assert
            verify(jpaRepository).save(entityCaptor.capture());
            PackageEntity savedEntity = entityCaptor.getValue();

            assertThat(savedEntity.getLocationHistory()).hasSize(1);
            LocationHistoryEntity locationEntity = savedEntity.getLocationHistory().get(0);
            assertThat(locationEntity.getCity()).isEqualTo("Origin City");
            assertThat(locationEntity.getCountry()).isEqualTo("Origin Country");
            assertThat(locationEntity.getDescription()).isEqualTo("Package created");
            assertThat(locationEntity.getLatitude()).isEqualTo(40.7128);
            assertThat(locationEntity.getLongitude()).isEqualTo(-74.0060);
        }
    }

    @Nested
    @DisplayName("Find Package Tests")
    class FindPackageTests {

        @Test
        @DisplayName("Should find package by ID successfully")
        void shouldFindPackageByIdSuccessfully() {
            // Arrange
            when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            Package foundPackage = result.get();
            assertThat(foundPackage.getId().getValue()).isEqualTo("LT-TEST12345");
            assertThat(foundPackage.getRecipient().getName()).isEqualTo("John Doe");

            verify(jpaRepository).findById("LT-TEST12345");
        }

        @Test
        @DisplayName("Should return empty when package not found")
        void shouldReturnEmptyWhenPackageNotFound() {
            // Arrange
            when(jpaRepository.findById("INVALID_ID")).thenReturn(Optional.empty());

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("INVALID_ID");

            // Assert
            assertThat(result).isEmpty();

            verify(jpaRepository).findById("INVALID_ID");
        }

        @Test
        @DisplayName("Should find package by ID and not deleted")
        void shouldFindPackageByIdAndNotDeleted() {
            // Arrange
            when(jpaRepository.findByIdAndDeletedFalse("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findByIdAndNotDeleted("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId().getValue()).isEqualTo("LT-TEST12345");
            assertThat(result.get().isDeleted()).isFalse();

            verify(jpaRepository).findByIdAndDeletedFalse("LT-TEST12345");
        }

        @Test
        @DisplayName("Should map entity to domain correctly")
        void shouldMapEntityToDomainCorrectly() {
            // Arrange
            when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            Package pkg = result.get();

            assertThat(pkg.getId().getValue()).isEqualTo("LT-TEST12345");
            assertThat(pkg.getRecipient().getName()).isEqualTo("John Doe");
            assertThat(pkg.getRecipient().getEmail()).isEqualTo("john@test.com");
            assertThat(pkg.getRecipient().getPhone()).isEqualTo("+1234567890");
            assertThat(pkg.getRecipient().getAddress().getStreet()).isEqualTo("123 Test St");
            assertThat(pkg.getDimensions().getHeight().doubleValue()).isEqualTo(20);
            assertThat(pkg.getDimensions().getWidth().doubleValue()).isEqualTo(15);
            assertThat(pkg.getDimensions().getDepth().doubleValue()).isEqualTo(10);
            assertThat(pkg.getWeight().toKilograms()).isEqualTo(2.5);
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);
            assertThat(pkg.getNotes()).isEqualTo("Test package");
        }
    }

    @Nested
    @DisplayName("Find By Status Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find packages by status")
        void shouldFindPackagesByStatus() {
            // Arrange
            List<PackageEntity> entities = Arrays.asList(testEntity);
            when(jpaRepository.findByStatusAndDeletedFalse(PackageStatus.CREATED)).thenReturn(entities);

            // Act
            List<Package> result = packageRepositoryAdapter.findByStatus(PackageStatus.CREATED);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId().getValue()).isEqualTo("LT-TEST12345");
            assertThat(result.get(0).getStatus()).isEqualTo(PackageStatus.CREATED);

            verify(jpaRepository).findByStatusAndDeletedFalse(PackageStatus.CREATED);
        }

        @Test
        @DisplayName("Should return empty list when no packages found by status")
        void shouldReturnEmptyListWhenNoPackagesFoundByStatus() {
            // Arrange
            when(jpaRepository.findByStatusAndDeletedFalse(PackageStatus.DELIVERED)).thenReturn(List.of());

            // Act
            List<Package> result = packageRepositoryAdapter.findByStatus(PackageStatus.DELIVERED);

            // Assert
            assertThat(result).isEmpty();

            verify(jpaRepository).findByStatusAndDeletedFalse(PackageStatus.DELIVERED);
        }
    }

    @Nested
    @DisplayName("Search Packages Tests")
    class SearchPackagesTests {

        @Test
        @DisplayName("Should search packages with criteria")
        void shouldSearchPackagesWithCriteria() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            PackageRepository.SearchCriteria criteria = new PackageRepository.SearchCriteria(
                    "John", "john@test.com", PackageStatus.CREATED, null, null, false
            );

            Page<PackageEntity> entityPage = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
            when(jpaRepository.searchPackages(anyString(), anyString(), any(PackageStatus.class),
                    any(), any(), anyBoolean(), eq(pageable)))
                    .thenReturn(entityPage);

            // Act
            Page<Package> result = packageRepositoryAdapter.search(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId().getValue()).isEqualTo("LT-TEST12345");

            verify(jpaRepository).searchPackages("John", "john@test.com", PackageStatus.CREATED,
                    null, null, false, pageable);
        }

        @Test
        @DisplayName("Should search packages including deleted")
        void shouldSearchPackagesIncludingDeleted() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            PackageRepository.SearchCriteria criteria = new PackageRepository.SearchCriteria(
                    null, null, null, null, null, true
            );

            Page<PackageEntity> entityPage = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
            when(jpaRepository.searchPackages(isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(true), eq(pageable)))
                    .thenReturn(entityPage);

            // Act
            Page<Package> result = packageRepositoryAdapter.search(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(jpaRepository).searchPackages(null, null, null, null, null, true, pageable);
        }

        @Test
        @DisplayName("Should search packages with date range")
        void shouldSearchPackagesWithDateRange() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();
            PackageRepository.SearchCriteria criteria = new PackageRepository.SearchCriteria(
                    null, null, null, from, to, false
            );

            Page<PackageEntity> entityPage = new PageImpl<>(Arrays.asList(testEntity), pageable, 1);
            when(jpaRepository.searchPackages(isNull(), isNull(), isNull(),
                    eq(from), eq(to), eq(false), eq(pageable)))
                    .thenReturn(entityPage);

            // Act
            Page<Package> result = packageRepositoryAdapter.search(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(jpaRepository).searchPackages(null, null, null, from, to, false, pageable);
        }
    }

    @Nested
    @DisplayName("Delete Package Tests")
    class DeletePackageTests {

        @Test
        @DisplayName("Should delete package by ID")
        void shouldDeletePackageById() {
            // Arrange
            doNothing().when(jpaRepository).deleteById("LT-TEST12345");

            // Act
            packageRepositoryAdapter.deleteById("LT-TEST12345");

            // Assert
            verify(jpaRepository).deleteById("LT-TEST12345");
        }

        @Test
        @DisplayName("Should check if package exists")
        void shouldCheckIfPackageExists() {
            // Arrange
            when(jpaRepository.existsById("LT-TEST12345")).thenReturn(true);

            // Act
            boolean result = packageRepositoryAdapter.existsById("LT-TEST12345");

            // Assert
            assertThat(result).isTrue();
            verify(jpaRepository).existsById("LT-TEST12345");
        }

        @Test
        @DisplayName("Should return false when package does not exist")
        void shouldReturnFalseWhenPackageDoesNotExist() {
            // Arrange
            when(jpaRepository.existsById("INVALID_ID")).thenReturn(false);

            // Act
            boolean result = packageRepositoryAdapter.existsById("INVALID_ID");

            // Assert
            assertThat(result).isFalse();
            verify(jpaRepository).existsById("INVALID_ID");
        }
    }

    @Nested
    @DisplayName("Location History Mapping Tests")
    class LocationHistoryMappingTests {

        @Test
        @DisplayName("Should map location history from entity to domain")
        void shouldMapLocationHistoryFromEntityToDomain() {
            // Arrange
            UUID locationId = UUID.randomUUID();
            LocationHistoryEntity locationEntity = LocationHistoryEntity.builder()
                    .id(locationId)
                    .city("Transit City")
                    .country("Transit Country")
                    .description("Package in transit")
                    .latitude(41.8781)
                    .longitude(-87.6298)
                    .timestamp(testDateTime)
                    .build();

            testEntity.getLocationHistory().add(locationEntity);
            when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            Package pkg = result.get();

            assertThat(pkg.getLocationHistory().getLocations()).hasSize(1);
            Location location = pkg.getLocationHistory().getLocations().get(0);
            assertThat(location.getId()).isEqualTo(locationId.toString());
            assertThat(location.getCity()).isEqualTo("Transit City");
            assertThat(location.getCountry()).isEqualTo("Transit Country");
            assertThat(location.getDescription()).isEqualTo("Package in transit");
            assertThat(location.getLatitude()).isEqualTo(41.8781);
            assertThat(location.getLongitude()).isEqualTo(-87.6298);
            assertThat(location.getTimestamp()).isEqualTo(testDateTime);
        }

        @Test
        @DisplayName("Should handle empty location history")
        void shouldHandleEmptyLocationHistory() {
            // Arrange
            when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            Package pkg = result.get();
            assertThat(pkg.getLocationHistory().getLocations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("State Mapping Tests")
    class StateMappingTests {

        @Test
        @DisplayName("Should create correct state for each package status")
        void shouldCreateCorrectStateForEachPackageStatus() {
            // Arrange & Act & Assert - Test all status mappings
            PackageStatus[] statuses = PackageStatus.values();

            for (PackageStatus status : statuses) {
                // Reset para cada iteración
                reset(jpaRepository);

                // Configurar entidad con el status específico
                PackageEntity entityWithStatus = PackageEntity.builder()
                        .id("LT-TEST12345")
                        .recipientName("John Doe")
                        .recipientEmail("john@test.com")
                        .recipientPhone("+1234567890")
                        .street("123 Test St")
                        .city("Test City")
                        .state("Test State")
                        .country("Test Country")
                        .postalCode("12345")
                        .height(BigDecimal.valueOf(20))
                        .width(BigDecimal.valueOf(15))
                        .depth(BigDecimal.valueOf(10))
                        .weight(BigDecimal.valueOf(2.5))
                        .status(status)
                        .notes("Test package")
                        .deleted(false)
                        .createdAt(testDateTime)
                        .updatedAt(testDateTime)
                        .locationHistory(new ArrayList<>())
                        .build();

                when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(entityWithStatus));

                // Act
                Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

                // Assert
                assertThat(result).isPresent();
                Package pkg = result.get();
                assertThat(pkg.getStatus()).isEqualTo(status);

                // Verificar comportamiento específico según el estado
                switch (status) {
                    case CREATED -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);
                        assertThat(pkg.canBeModified()).isTrue(); // canTransition = true
                        assertThat(pkg.isDelivered()).isFalse();
                    }
                    case IN_TRANSIT -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
                        assertThat(pkg.canBeModified()).isTrue(); // canTransition = true
                        assertThat(pkg.isDelivered()).isFalse();
                    }
                    case OUT_FOR_DELIVERY -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
                        assertThat(pkg.canBeModified()).isTrue(); // canTransition = true
                        assertThat(pkg.isDelivered()).isFalse();
                    }
                    case DELIVERED -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.DELIVERED);
                        assertThat(pkg.isDelivered()).isTrue();
                        assertThat(pkg.canBeModified()).isFalse(); // canTransition = false (estado final)
                    }
                    case DELIVERY_FAILED -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.DELIVERY_FAILED);
                        assertThat(pkg.canBeModified()).isTrue(); // canTransition = true (puede retry)
                        assertThat(pkg.isDelivered()).isFalse();
                    }
                    case RETURNED -> {
                        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.RETURNED);
                        assertThat(pkg.canBeModified()).isFalse(); // canTransition = false (estado final)
                        assertThat(pkg.isDelivered()).isFalse();
                    }
                }
            }
        }

        @Test
        @DisplayName("Should handle state transitions correctly after mapping")
        void shouldHandleStateTransitionsCorrectlyAfterMapping() {
            // Arrange
            testEntity.setStatus(PackageStatus.CREATED);
            when(jpaRepository.findById("LT-TEST12345")).thenReturn(Optional.of(testEntity));

            // Act
            Optional<Package> result = packageRepositoryAdapter.findById("LT-TEST12345");

            // Assert
            assertThat(result).isPresent();
            Package pkg = result.get();
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.CREATED);

            // Verificar que el estado permite transiciones apropiadas
            assertThatCode(() -> pkg.changeStatus(PackageStatus.IN_TRANSIT))
                    .doesNotThrowAnyException();

            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
        }
    }
}

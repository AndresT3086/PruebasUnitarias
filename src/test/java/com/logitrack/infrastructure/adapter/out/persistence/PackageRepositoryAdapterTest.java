package com.logitrack.infrastructure.adapter.out.persistence;
import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import com.logitrack.infrastructure.adapter.out.persistence.entity.PackageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageRepositoryAdapterTest {

    @Mock
    private PackageJpaRepository jpaRepository;

    @InjectMocks
    private PackageRepositoryAdapter repositoryAdapter;

    private Package domainPackage;
    private PackageEntity packageEntity;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        Recipient.Address address = Recipient.Address.builder()
                .street("123 Test St")
                .city("Test City")
                .country("Test Country")
                .postalCode("12345")
                .build();

        Recipient recipient = Recipient.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("+1234567890")
                .address(address)
                .build();

        domainPackage = Package.builder()
                .id(PackageId.of("LT-TEST12345"))
                .recipient(recipient)
                .dimensions(Dimensions.of(20, 15, 10))
                .weight(Weight.ofKilograms(2.5))
                .build();

        packageEntity = PackageEntity.builder()
                .id("LT-TEST12345")
                .recipientName("Test User")
                .recipientEmail("test@example.com")
                .recipientPhone("+1234567890")
                .street("123 Test St")
                .city("Test City")
                .country("Test Country")
                .postalCode("12345")
                .height(new BigDecimal("20.00"))
                .width(new BigDecimal("15.00"))
                .depth(new BigDecimal("10.00"))
                .weight(new BigDecimal("2.500"))
                .status(PackageStatus.CREATED)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Should save package successfully")
    void shouldSavePackageSuccessfully() {
        // Arrange
        when(jpaRepository.save(any(PackageEntity.class)))
                .thenReturn(packageEntity);

        // Act
        Package savedPackage = repositoryAdapter.save(domainPackage);

        // Assert
        assertThat(savedPackage).isNotNull();
        assertThat(savedPackage.getId().getValue()).isEqualTo("LT-TEST12345");
        verify(jpaRepository, times(1)).save(any(PackageEntity.class));
    }

    @Test
    @DisplayName("Should find package by ID")
    void shouldFindPackageById() {
        // Arrange
        when(jpaRepository.findById("LT-TEST12345"))
                .thenReturn(Optional.of(packageEntity));

        // Act
        Optional<Package> found = repositoryAdapter.findById("LT-TEST12345");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId().getValue()).isEqualTo("LT-TEST12345");
        assertThat(found.get().getRecipient().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should return empty when package not found")
    void shouldReturnEmptyWhenPackageNotFound() {
        // Arrange
        when(jpaRepository.findById("INVALID"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Package> found = repositoryAdapter.findById("INVALID");

        // Assert
        assertThat(found).isEmpty();
    }
}

package com.logitrack.application.service;

import com.logitrack.application.dto.*;
import com.logitrack.application.factory.PackageFactory;
import com.logitrack.domain.event.DomainEvent;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.domain.model.*;
import com.logitrack.domain.port.in.*;
import com.logitrack.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.logitrack.domain.model.Package;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PackageServiceImpl implements PackageService,
        CreatePackageUseCase, UpdatePackageStatusUseCase,
        AddLocationUseCase, GetPackageUseCase, SearchPackagesUseCase {

    private final PackageRepository packageRepository;
    private final EventPublisher eventPublisher;
    private final LocationService locationService;
    private final PackageFactory packageFactory;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public PackageResponse createPackage(com.logitrack.application.dto.CreatePackageCommand command) {
        log.debug("Creating package for recipient: {}", command.getRecipientEmail());

        // Obtener coordenadas reales
        LocationService.LocationInfo locationInfo = locationService
                .getLocationInfo(command.getCity(), command.getCountry())
                .orElse(null);

        if (locationInfo == null) {
            log.warn("Could not geocode location: {}, {}", command.getCity(), command.getCountry());
        }

        Package pkg = packageFactory.createPackage(command);

        Location initialLocation = Location.create(
                locationInfo != null ? locationInfo.city()      : command.getCity(),
                locationInfo != null ? locationInfo.country()   : command.getCountry(),
                "Package created",
                locationInfo != null ? locationInfo.latitude()  : null,
                locationInfo != null ? locationInfo.longitude() : null
        );
        pkg.addLocation(initialLocation);

        Package savedPackage = packageRepository.save(pkg);
        publishEvents(savedPackage);

        log.info("Package created successfully with ID: {}", savedPackage.getId());
        return toResponse(savedPackage);
    }

    @Override
    public Package createPackage(CreatePackageUseCase.CreatePackageCommand command) {
        com.logitrack.application.dto.CreatePackageCommand dto =
                com.logitrack.application.dto.CreatePackageCommand.builder()
                        .recipientName(command.recipientName())
                        .recipientEmail(command.recipientEmail())
                        .recipientPhone(command.recipientPhone())
                        .street(command.street())
                        .city(command.city())
                        .state(command.state())
                        .country(command.country())
                        .postalCode(command.postalCode())
                        .height(command.height())
                        .width(command.width())
                        .depth(command.depth())
                        .weight(command.weight())
                        .notes(command.notes())
                        .build();

        PackageResponse response = createPackage(dto);

        return packageRepository.findById(response.getId())
                .orElseThrow(() -> new PackageNotFoundException(response.getId()));
    }

    @Override
    public PackageResponse updateStatus(String packageId, PackageStatus newStatus) {
        log.debug("Updating package {} status to {}", packageId, newStatus);

        Package pkg = getPackageOrThrow(packageId);
        PackageStatus oldStatus = pkg.getStatus();

        pkg.changeStatus(newStatus);
        Package updatedPackage = packageRepository.save(pkg);

        publishEvents(updatedPackage);

        log.info("Package {} status updated from {} to {}",
                packageId, oldStatus, newStatus);
        return toResponse(updatedPackage);
    }

    @Override
    public Package updatePackageStatus(String packageId, PackageStatus newStatus) {
        return packageRepository.findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));
    }

    @Override
    public PackageResponse addLocation(com.logitrack.application.dto.AddLocationCommand command) {
        log.debug("Adding location to package {}", command.getPackageId());

        Package pkg = getPackageOrThrow(command.getPackageId());

        LocationService.LocationInfo locationInfo = null;
        if (command.getLatitude() != null && command.getLongitude() != null) {
            locationInfo = locationService.getLocationByCoordinates(
                    command.getLatitude(),
                    command.getLongitude()
            ).orElse(null);
        } else {
            validateLocation(command.getCity(), command.getCountry());
        }

        Location location = Location.create(
                locationInfo != null ? locationInfo.city() : command.getCity(),
                locationInfo != null ? locationInfo.country() : command.getCountry(),
                command.getDescription(),
                command.getLatitude(),
                command.getLongitude()
        );

        pkg.addLocation(location);
        Package updatedPackage = packageRepository.save(pkg);

        publishEvents(updatedPackage);

        log.info("Location added to package {}", command.getPackageId());
        return toResponse(updatedPackage);
    }

    @Override
    public Package addLocation(AddLocationUseCase.AddLocationCommand command) {
        com.logitrack.application.dto.AddLocationCommand dto =
                com.logitrack.application.dto.AddLocationCommand.builder()
                        .packageId(command.packageId())
                        .city(command.city())
                        .country(command.country())
                        .description(command.description())
                        .latitude(command.latitude())
                        .longitude(command.longitude())
                        .build();

        addLocation(dto);
        return packageRepository.findById(command.packageId())
                .orElseThrow(() -> new PackageNotFoundException(command.packageId()));
    }

    @Override
    public Optional<PackageResponse> findById(String packageId) {
        return packageRepository.findByIdAndNotDeleted(packageId)
                .map(this::toResponse);
    }

    @Override
    public Optional<Package> getPackage(String packageId) {
        return packageRepository.findByIdAndNotDeleted(packageId);
    }

    @Override
    public PackageResponse getByIdOrThrow(String packageId) {
        return findById(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));
    }

    @Override
    public Package getPackageOrThrow(String packageId) {
        return packageRepository.findByIdAndNotDeleted(packageId)
                .orElseThrow(() -> new PackageNotFoundException(packageId));
    }

    @Override
    public Page<PackageResponse> searchPackages(PackageService.SearchCriteria criteria,
                                                Pageable pageable) {
        LocalDateTime dateFrom = criteria.dateFrom() != null ?
                LocalDateTime.parse(criteria.dateFrom(), DATE_FORMATTER) : null;
        LocalDateTime dateTo = criteria.dateTo() != null ?
                LocalDateTime.parse(criteria.dateTo(), DATE_FORMATTER) : null;

        PackageRepository.SearchCriteria repoCriteria =
                new PackageRepository.SearchCriteria(
                        criteria.recipientName(),
                        criteria.recipientEmail(),
                        criteria.status(),
                        dateFrom,
                        dateTo,
                        criteria.includeDeleted()
                );

        return packageRepository.search(repoCriteria, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<Package> searchPackages(SearchPackagesUseCase.SearchCriteria criteria,
                                        Pageable pageable) {
        PackageRepository.SearchCriteria repoCriteria =
                new PackageRepository.SearchCriteria(
                        criteria.recipientName(),
                        criteria.recipientEmail(),
                        criteria.status(),
                        criteria.createdFrom(),
                        criteria.createdTo(),
                        criteria.deleted()
                );

        return packageRepository.search(repoCriteria, pageable);
    }

    @Override
    public List<PackageResponse> findByStatus(PackageStatus status) {
        return packageRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Package> getPackagesByStatus(PackageStatus status) {
        return packageRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void deletePackage(String packageId) {
        log.debug("Soft deleting package {}", packageId);

        Package pkg = getPackageOrThrow(packageId);
        pkg.softDelete();
        packageRepository.save(pkg);

        log.info("Package {} soft deleted", packageId);
    }

    private void validateLocation(String city, String country) {
        if (!locationService.validateLocation(city, country)) {
            log.warn("Invalid location: {}, {}", city, country);
        }
    }

    private void publishEvents(Package pkg) {
        List<DomainEvent> events = pkg.getAndClearEvents();
        events.forEach(event -> {
            try {
                eventPublisher.publish("package-events", event);
                log.debug("Published event: {}", event.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish event: {}", event.getEventType(), e);
            }
        });
    }

    private PackageResponse toResponse(Package pkg) {
        PackageResponse.RecipientDto recipientDto = PackageResponse.RecipientDto.builder()
                .name(pkg.getRecipient().getName())
                .email(pkg.getRecipient().getEmail())
                .phone(pkg.getRecipient().getPhone())
                .address(PackageResponse.AddressDto.builder()
                        .street(pkg.getRecipient().getAddress().getStreet())
                        .city(pkg.getRecipient().getAddress().getCity())
                        .state(pkg.getRecipient().getAddress().getState())
                        .country(pkg.getRecipient().getAddress().getCountry())
                        .postalCode(pkg.getRecipient().getAddress().getPostalCode())
                        .fullAddress(pkg.getRecipient().getAddress().getFullAddress())
                        .build())
                .build();

        PackageResponse.DimensionsDto dimensionsDto = PackageResponse.DimensionsDto.builder()
                .height(pkg.getDimensions().getHeight().doubleValue())
                .width(pkg.getDimensions().getWidth().doubleValue())
                .depth(pkg.getDimensions().getDepth().doubleValue())
                .volume(pkg.getDimensions().calculateVolume().doubleValue())
                .build();

        List<PackageResponse.LocationDto> locationDtos = pkg.getLocationHistory()
                .getLocations().stream()
                .map(loc -> PackageResponse.LocationDto.builder()
                        .city(loc.getCity())
                        .country(loc.getCountry())
                        .description(loc.getDescription())
                        .timestamp(loc.getTimestamp())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .build())
                .collect(Collectors.toList());

        String currentLocation = pkg.getCurrentLocation()
                .map(Location::getFormattedLocation)
                .orElse("No location registered");

        return PackageResponse.builder()
                .id(pkg.getId().getValue())
                .recipient(recipientDto)
                .dimensions(dimensionsDto)
                .weight(pkg.getWeight().toKilograms())
                .status(pkg.getStatus())
                .locations(locationDtos)
                .currentLocation(currentLocation)
                .notes(pkg.getNotes())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .build();
    }
}
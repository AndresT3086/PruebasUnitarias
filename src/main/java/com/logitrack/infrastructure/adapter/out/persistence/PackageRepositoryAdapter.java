package com.logitrack.infrastructure.adapter.out.persistence;

import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.state.*;
import com.logitrack.domain.port.out.PackageRepository;
import com.logitrack.infrastructure.adapter.out.persistence.entity.LocationHistoryEntity;
import com.logitrack.infrastructure.adapter.out.persistence.entity.PackageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PackageRepositoryAdapter implements PackageRepository {


    private final PackageJpaRepository jpaRepository;

    @Override
    public Package save(Package domainPackage) {
        PackageEntity entity = toEntity(domainPackage);
        PackageEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Package> findById(String packageId) {
        return jpaRepository.findById(packageId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Package> findByIdAndNotDeleted(String packageId) {
        return jpaRepository.findByIdAndDeletedFalse(packageId)
                .map(this::toDomain);
    }

    @Override
    public List<Package> findByStatus(PackageStatus status) {
        return jpaRepository.findByStatusAndDeletedFalse(status)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Page<Package> search(SearchCriteria criteria, Pageable pageable) {
        PackageStatus statusEnum = criteria.status();
        boolean includeDeleted = criteria.deleted() != null && criteria.deleted();

        Page<PackageEntity> entities = jpaRepository.searchPackages(
                criteria.recipientName(),
                criteria.recipientEmail(),
                statusEnum,
                criteria.createdFrom(),
                criteria.createdTo(),
                includeDeleted,
                pageable
        );

        return entities.map(this::toDomain);
    }

    @Override
    public void deleteById(String packageId) {
        jpaRepository.deleteById(packageId);
    }

    @Override
    public boolean existsById(String packageId) {
        return jpaRepository.existsById(packageId);
    }

    private PackageEntity toEntity(Package domainPackage) {
        PackageEntity entity = PackageEntity.builder()
                .id(domainPackage.getId().getValue())
                .recipientName(domainPackage.getRecipient().getName())
                .recipientEmail(domainPackage.getRecipient().getEmail())
                .recipientPhone(domainPackage.getRecipient().getPhone())
                .street(domainPackage.getRecipient().getAddress().getStreet())
                .city(domainPackage.getRecipient().getAddress().getCity())
                .state(domainPackage.getRecipient().getAddress().getState())
                .country(domainPackage.getRecipient().getAddress().getCountry())
                .postalCode(domainPackage.getRecipient().getAddress().getPostalCode())
                .height(domainPackage.getDimensions().getHeight())
                .width(domainPackage.getDimensions().getWidth())
                .depth(domainPackage.getDimensions().getDepth())
                .weight(domainPackage.getWeight().getValueInKg())
                .status(domainPackage.getStatus())
                .notes(domainPackage.getNotes())
                .deleted(domainPackage.isDeleted())
                .createdAt(domainPackage.getCreatedAt())
                .updatedAt(domainPackage.getUpdatedAt())
                .build();

        domainPackage.getLocationHistory().getLocations().forEach(location -> {
            LocationHistoryEntity locationEntity = LocationHistoryEntity.builder()
                    .id(UUID.fromString(location.getId()))
                    .city(location.getCity())
                    .country(location.getCountry())
                    .description(location.getDescription())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .timestamp(location.getTimestamp())
                    .build();
            entity.addLocation(locationEntity);
        });

        return entity;
    }

    private Package toDomain(PackageEntity entity) {

        Recipient.Address address = Recipient.Address.builder()
                .street(entity.getStreet())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .build();

        Recipient recipient = Recipient.builder()
                .name(entity.getRecipientName())
                .email(entity.getRecipientEmail())
                .phone(entity.getRecipientPhone())
                .address(address)
                .build();

        Dimensions dimensions = Dimensions.of(
                entity.getHeight().doubleValue(),
                entity.getWidth().doubleValue(),
                entity.getDepth().doubleValue()
        );

        Weight weight = Weight.ofKilograms(entity.getWeight().doubleValue());

        PackageStatus status = PackageStatus.valueOf(entity.getStatus().name());
        PackageState state = createState(status);

        Package pkg = PackageReconstitutor.reconstitute(
                PackageId.of(entity.getId()),
                recipient,
                dimensions,
                weight,
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted(),
                state
        );

        entity.getLocationHistory().forEach(locationEntity -> {
            Location location = Location.builder()
                    .id(locationEntity.getId().toString())
                    .city(locationEntity.getCity())
                    .country(locationEntity.getCountry())
                    .description(locationEntity.getDescription())
                    .latitude(locationEntity.getLatitude())
                    .longitude(locationEntity.getLongitude())
                    .timestamp(locationEntity.getTimestamp())
                    .build();

            pkg.getLocationHistory().addLocation(location);
        });

        return pkg;
    }

    private PackageState createState(PackageStatus status) {
        return switch (status) {
            case CREATED -> new CreatedState();
            case IN_TRANSIT -> new InTransitState();
            case OUT_FOR_DELIVERY -> new OutForDeliveryState();
            case DELIVERED -> new DeliveredState();
            case DELIVERY_FAILED -> new DeliveryFailedState();
            case RETURNED -> new ReturnedState();
        };
    }
}

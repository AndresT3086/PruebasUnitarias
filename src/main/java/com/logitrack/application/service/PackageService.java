package com.logitrack.application.service;

import com.logitrack.application.dto.AddLocationCommand;
import com.logitrack.application.dto.CreatePackageCommand;
import com.logitrack.application.dto.PackageResponse;
import com.logitrack.domain.model.PackageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PackageService {

    PackageResponse createPackage(CreatePackageCommand command);
    PackageResponse updateStatus(String packageId, PackageStatus newStatus);
    PackageResponse addLocation(AddLocationCommand command);
    Optional<PackageResponse> findById(String packageId);
    PackageResponse getByIdOrThrow(String packageId);
    Page<PackageResponse> searchPackages(SearchCriteria criteria, Pageable pageable);
    List<PackageResponse> findByStatus(PackageStatus status);
    void deletePackage(String packageId);

    record SearchCriteria(
            String recipientName,
            String recipientEmail,
            PackageStatus status,
            String dateFrom,
            String dateTo,
            Boolean includeDeleted
    ) {}
}

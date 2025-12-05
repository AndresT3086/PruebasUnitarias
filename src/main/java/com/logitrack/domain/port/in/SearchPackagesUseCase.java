package com.logitrack.domain.port.in;

import com.logitrack.domain.model.PackageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.logitrack.domain.model.Package;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchPackagesUseCase {

    Page<Package> searchPackages(SearchCriteria criteria, Pageable pageable);
    List<Package> getPackagesByStatus(PackageStatus status);

    record SearchCriteria(
            String recipientName,
            String recipientEmail,
            PackageStatus status,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Boolean deleted
    ) {}
}
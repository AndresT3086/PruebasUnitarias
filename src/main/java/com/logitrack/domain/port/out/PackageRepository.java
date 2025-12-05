package com.logitrack.domain.port.out;

import com.logitrack.domain.model.PackageStatus;
import com.logitrack.domain.model.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PackageRepository {
    Package save(Package packageEntity);
    Optional<Package> findById(String packageId);
    Optional<Package> findByIdAndNotDeleted(String packageId);
    List<Package> findByStatus(PackageStatus status);
    Page<Package> search(SearchCriteria criteria, Pageable pageable);
    void deleteById(String packageId);
    boolean existsById(String packageId);

    record SearchCriteria(
            String recipientName,
            String recipientEmail,
            PackageStatus status,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Boolean deleted
    ) {}
}
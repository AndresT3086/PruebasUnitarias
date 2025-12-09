package com.logitrack.infrastructure.adapter.out.persistence;

import com.logitrack.domain.model.PackageStatus;
import com.logitrack.infrastructure.adapter.out.persistence.entity.PackageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PackageJpaRepository extends JpaRepository<PackageEntity, String> {

    Optional<PackageEntity> findByIdAndDeletedFalse(String id);

    List<PackageEntity> findByStatusAndDeletedFalse(PackageStatus status);

    @Query("""
        SELECT p FROM PackageEntity p
        WHERE (:recipientName IS NULL OR LOWER(p.recipientName) LIKE LOWER(CONCAT('%', :recipientName, '%')))
        AND (:recipientEmail IS NULL OR LOWER(p.recipientEmail) = LOWER(:recipientEmail))
        AND (:status IS NULL OR p.status = :status)
        AND (:createdFrom IS NULL OR p.createdAt >= :createdFrom)
        AND (:createdTo IS NULL OR p.createdAt <= :createdTo)
        AND (:includeDeleted = true OR p.deleted = false)
        """)
    Page<PackageEntity> searchPackages(
            @Param("recipientName") String recipientName,
            @Param("recipientEmail") String recipientEmail,
            @Param("status") PackageStatus status,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );
}

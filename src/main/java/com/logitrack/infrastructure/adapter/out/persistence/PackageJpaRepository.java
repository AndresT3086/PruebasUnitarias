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

    @Query(value = """
    SELECT * FROM packages p
    WHERE (:recipientName IS NULL OR p.recipient_name ILIKE CONCAT('%', CAST(:recipientName AS VARCHAR), '%'))
    AND (:recipientEmail IS NULL OR p.recipient_email ILIKE CAST(:recipientEmail AS VARCHAR))
    AND (CAST(:status AS VARCHAR) IS NULL OR p.status = CAST(:status AS VARCHAR))
    AND (CAST(:createdFrom AS TIMESTAMP) IS NULL OR p.created_at >= CAST(:createdFrom AS TIMESTAMP))
    AND (CAST(:createdTo AS TIMESTAMP) IS NULL OR p.created_at <= CAST(:createdTo AS TIMESTAMP))
    AND (:includeDeleted = true OR p.deleted = false)
    ORDER BY p.created_at DESC
    """,
            countQuery = """
    SELECT COUNT(*) FROM packages p
    WHERE (:recipientName IS NULL OR p.recipient_name ILIKE CONCAT('%', CAST(:recipientName AS VARCHAR), '%'))
    AND (:recipientEmail IS NULL OR p.recipient_email ILIKE CAST(:recipientEmail AS VARCHAR))
    AND (CAST(:status AS VARCHAR) IS NULL OR p.status = CAST(:status AS VARCHAR))
    AND (CAST(:createdFrom AS TIMESTAMP) IS NULL OR p.created_at >= CAST(:createdFrom AS TIMESTAMP))
    AND (CAST(:createdTo AS TIMESTAMP) IS NULL OR p.created_at <= CAST(:createdTo AS TIMESTAMP))
    AND (:includeDeleted = true OR p.deleted = false)
    """,
            nativeQuery = true)
    Page<PackageEntity> searchPackages(
            @Param("recipientName") String recipientName,
            @Param("recipientEmail") String recipientEmail,
            @Param("status") String status,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );
}

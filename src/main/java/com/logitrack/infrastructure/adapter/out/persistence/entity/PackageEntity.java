package com.logitrack.infrastructure.adapter.out.persistence.entity;

import com.logitrack.domain.model.PackageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages", indexes = {
        @Index(name = "idx_package_status", columnList = "status"),
        @Index(name = "idx_package_recipient_email", columnList = "recipient_email"),
        @Index(name = "idx_package_created_at", columnList = "created_at"),
        @Index(name = "idx_package_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageEntity {

    @Id
    @Column(name = "id", length = 12)
    private String id;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "height", nullable = false, precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "width", nullable = false, precision = 10, scale = 2)
    private BigDecimal width;

    @Column(name = "depth", nullable = false, precision = 10, scale = 2)
    private BigDecimal depth;

    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "packageEntity", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    @Builder.Default
    private List<LocationHistoryEntity> locationHistory = new ArrayList<>();

    public void addLocation(LocationHistoryEntity location) {
        locationHistory.add(location);
        location.setPackageEntity(this);
    }
}

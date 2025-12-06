package com.logitrack.domain.model;

import com.logitrack.domain.model.state.PackageState;

import java.time.LocalDateTime;

public class PackageReconstitutor {

    public static Package reconstitute(
            PackageId id,
            Recipient recipient,
            Dimensions dimensions,
            Weight weight,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted,
            PackageState state
    ) {
        Package pkg = Package.builder()
                .id(id)
                .recipient(recipient)
                .dimensions(dimensions)
                .weight(weight)
                .notes(notes)
                .build();

        pkg.setCreatedAt(createdAt);
        pkg.setUpdatedAt(updatedAt);
        pkg.setDeleted(deleted);
        pkg.applyState(state);

        return pkg;
    }
}

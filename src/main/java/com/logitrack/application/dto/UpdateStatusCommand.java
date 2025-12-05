package com.logitrack.application.dto;

import com.logitrack.domain.model.PackageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateStatusCommand {
    @NotNull(message = "Package ID is required")
    private String packageId;

    @NotNull(message = "Status is required")
    private PackageStatus status;

    private String reason;
}

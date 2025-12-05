package com.logitrack.domain.port.in;

import com.logitrack.domain.model.PackageStatus;

public interface UpdatePackageStatusUseCase {

    Package updateStatus(String packageId, PackageStatus newStatus);
}
package com.logitrack.domain.port.in;

import com.logitrack.domain.model.PackageStatus;
import com.logitrack.domain.model.Package;

public interface UpdatePackageStatusUseCase {

    Package updatePackageStatus(String packageId, PackageStatus newStatus);
}
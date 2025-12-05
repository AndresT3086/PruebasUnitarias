package com.logitrack.domain.port.in;

import java.util.Optional;
import com.logitrack.domain.model.Package;

public interface GetPackageUseCase {

    Optional<Package> getPackage(String packageId);
    Package getPackageOrThrow(String packageId);
}
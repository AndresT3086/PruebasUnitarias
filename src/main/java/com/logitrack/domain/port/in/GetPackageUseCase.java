package com.logitrack.domain.port.in;

import java.util.Optional;

public interface GetPackageUseCase {

    Optional<Package> getPackage(String packageId);
    Package getPackageOrThrow(String packageId);
}
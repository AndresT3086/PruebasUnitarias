package com.logitrack.domain.exception;

public class PackageNotFoundException extends DomainException {

    public PackageNotFoundException(String packageId) {
        super("Package not found:"+ packageId, "PACKAGE_NOT_FOUND");
    }
}

package com.logitrack.domain.exception;

public class InvalidPackageDataException extends DomainException {

    public InvalidPackageDataException(String message) {
        super(message,  "INVALID_PACKAGE_DATA");
    }
}

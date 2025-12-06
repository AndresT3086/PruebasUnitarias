package com.logitrack.domain.port.in;

import com.logitrack.domain.model.Package;

public interface CreatePackageUseCase {

    Package createPackage(CreatePackageCommand command);

    record CreatePackageCommand(
            String recipientName,
            String recipientEmail,
            String recipientPhone,
            String street,
            String city,
            String state,
            String country,
            String postalCode,
            double height,
            double width,
            double depth,
            double weight,
            String notes
    ) {}
}

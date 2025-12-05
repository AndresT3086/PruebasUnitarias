package com.logitrack.domain.port.in;

import com.logitrack.domain.model.Package;

public interface AddLocationUseCase {

    Package addLocation(AddLocationCommand command);

    record AddLocationCommand(
            String packageId,
            String city,
            String country,
            String description,
            Double latitude,
            Double longitude
    ) {}
}

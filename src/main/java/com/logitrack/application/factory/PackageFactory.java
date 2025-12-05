package com.logitrack.application.factory;

import com.logitrack.domain.model.*;
import com.logitrack.application.dto.CreatePackageCommand;
import com.logitrack.domain.model.Package;
import org.springframework.stereotype.Component;

@Component
public class PackageFactory {

    public Package createPackage(CreatePackageCommand command) {
        Recipient.Address address = Recipient.Address.builder()
                .street(command.getStreet())
                .city(command.getCity())
                .state(command.getState())
                .country(command.getCountry())
                .postalCode(command.getPostalCode())
                .build();

        Recipient recipient = Recipient.builder()
                .name(command.getRecipientName())
                .email(command.getRecipientEmail())
                .phone(command.getRecipientPhone())
                .address(address)
                .build();

        Dimensions dimensions = Dimensions.of(
                command.getHeight(),
                command.getWidth(),
                command.getDepth()
        );

        Weight weight = Weight.ofKilograms(command.getWeight());

        return Package.builder()
                .recipient(recipient)
                .dimensions(dimensions)
                .weight(weight)
                .notes(command.getNotes())
                .build();
    }
}

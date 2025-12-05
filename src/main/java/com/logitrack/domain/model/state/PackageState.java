package com.logitrack.domain.model.state;

import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public interface PackageState {

    PackageStatus getStatus();

    void toInTransit(Package pkg);
    void toOutForDelivery(Package pkg);
    void toDelivered(Package pkg);
    void toDeliveryFailed(Package pkg);
    void toReturned(Package pkg);

    default boolean canTransitionTo(PackageStatus status) {
        return false;
    }
}

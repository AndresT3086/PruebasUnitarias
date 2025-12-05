package com.logitrack.domain.model.state;

import com.logitrack.domain.model.PackageStatus;
import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;

public class InTransitState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.IN_TRANSIT;
    }

    @Override
    public void toInTransit(Package pkg) {
        // Already in transit
    }

    @Override
    public void toOutForDelivery(Package pkg) {
        pkg.applyState(new OutForDeliveryState());
    }

    @Override
    public void toDelivered(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from IN_TRANSIT to DELIVERED directly"
        );
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from IN_TRANSIT to DELIVERY_FAILED"
        );
    }

    @Override
    public void toReturned(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from IN_TRANSIT to RETURNED directly"
        );
    }

    @Override
    public boolean canTransitionTo(PackageStatus status) {
        return status == PackageStatus.OUT_FOR_DELIVERY;
    }
}

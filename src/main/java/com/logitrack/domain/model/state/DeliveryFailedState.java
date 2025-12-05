package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public class DeliveryFailedState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.DELIVERY_FAILED;
    }

    @Override
    public void toInTransit(Package pkg) {
        pkg.applyState(new InTransitState());
    }

    @Override
    public void toOutForDelivery(Package pkg) {
        throw new InvalidStateTransitionException(
                "Must go through IN_TRANSIT before OUT_FOR_DELIVERY"
        );
    }

    @Override
    public void toDelivered(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot deliver a failed package directly"
        );
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        // Already failed
    }

    @Override
    public void toReturned(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot return directly from DELIVERY_FAILED"
        );
    }

    @Override
    public boolean canTransitionTo(PackageStatus status) {
        return status == PackageStatus.IN_TRANSIT;
    }
}

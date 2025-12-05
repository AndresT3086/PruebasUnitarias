package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public class OutForDeliveryState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.OUT_FOR_DELIVERY;
    }

    @Override
    public void toInTransit(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from OUT_FOR_DELIVERY back to IN_TRANSIT"
        );
    }

    @Override
    public void toOutForDelivery(Package pkg) {
        // Already out for delivery
    }

    @Override
    public void toDelivered(Package pkg) {
        pkg.applyState(new DeliveredState());
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        pkg.applyState(new DeliveryFailedState());
    }

    @Override
    public void toReturned(Package pkg) {
        pkg.applyState(new ReturnedState());
    }

    @Override
    public boolean canTransitionTo(PackageStatus status) {
        return status == PackageStatus.DELIVERED ||
                status == PackageStatus.DELIVERY_FAILED ||
                status == PackageStatus.RETURNED;
    }
}

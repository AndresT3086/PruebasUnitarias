package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public class ReturnedState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.RETURNED;
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
        throw new InvalidStateTransitionException("Returned package cannot be delivered");
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        throw new InvalidStateTransitionException("Returned package cannot fail delivery");
    }

    @Override
    public void toReturned(Package pkg) {
        // Already returned
    }

    @Override
    public boolean canTransitionTo(PackageStatus status) {
        return status == PackageStatus.IN_TRANSIT;
    }
}

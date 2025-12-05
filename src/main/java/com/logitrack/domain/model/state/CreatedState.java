package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public class CreatedState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.CREATED;
    }

    @Override
    public void toInTransit(Package pkg) {
        pkg.applyState(new InTransitState());
    }

    @Override
    public void toOutForDelivery(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from CREATED to OUT_FOR_DELIVERY directly"
        );
    }

    @Override
    public void toDelivered(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from CREATED to DELIVERED directly"
        );
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from CREATED to DELIVERY_FAILED"
        );
    }

    @Override
    public void toReturned(Package pkg) {
        throw new InvalidStateTransitionException(
                "Cannot transition from CREATED to RETURNED"
        );
    }

    @Override
    public boolean canTransitionTo(PackageStatus status) {
        return status == PackageStatus.IN_TRANSIT;
    }
}

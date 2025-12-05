package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

public class DeliveredState implements PackageState{

    @Override
    public PackageStatus getStatus() {
        return PackageStatus.DELIVERED;
    }

    @Override
    public void toInTransit(Package pkg) {
        throw new InvalidStateTransitionException("Package has been delivered, cannot change status");
    }

    @Override
    public void toOutForDelivery(Package pkg) {
        throw new InvalidStateTransitionException("Package has been delivered, cannot change status");
    }

    @Override
    public void toDelivered(Package pkg) {
        // Already delivered, no-op
    }

    @Override
    public void toDeliveryFailed(Package pkg) {
        throw new InvalidStateTransitionException("Package has been delivered, cannot change status");
    }

    @Override
    public void toReturned(Package pkg) {
        throw new InvalidStateTransitionException("Package has been delivered, cannot change status");
    }
}

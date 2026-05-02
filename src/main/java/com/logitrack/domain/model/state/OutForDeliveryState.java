package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;

import java.util.EnumSet;
import java.util.Set;

public class OutForDeliveryState implements PackageState{

    private static final Set<PackageStatus> ALLOWED_TRANSITIONS =
            EnumSet.of(PackageStatus.DELIVERED, PackageStatus.DELIVERY_FAILED, PackageStatus.RETURNED);

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
        return ALLOWED_TRANSITIONS.contains(status);
    }
}

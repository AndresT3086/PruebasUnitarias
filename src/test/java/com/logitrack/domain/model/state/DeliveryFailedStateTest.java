package com.logitrack.domain.model.state;

import com.logitrack.domain.exception.InvalidStateTransitionException;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.model.PackageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryFailedState Tests")
class DeliveryFailedStateTest {

    @Mock
    private Package mockPackage;

    private DeliveryFailedState deliveryFailedState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        deliveryFailedState = new DeliveryFailedState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return DELIVERY_FAILED status")
        void shouldReturnDeliveryFailedStatus() {
            // Arrange - deliveryFailedState already set up

            // Act
            PackageStatus result = deliveryFailedState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.DELIVERY_FAILED);
        }

        @Test
        @DisplayName("Should only allow transition to IN_TRANSIT")
        void shouldOnlyAllowTransitionToInTransit() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.RETURNED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Valid Transition Tests")
    class ValidTransitionTests {

        @Test
        @DisplayName("Should transition to IN_TRANSIT successfully")
        void shouldTransitionToInTransitSuccessfully() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(InTransitState.class));

            // Act
            deliveryFailedState.toInTransit(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(InTransitState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to IN_TRANSIT")
        void shouldApplyCorrectStateTypeWhenTransitioningToInTransit() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(InTransitState.class));

            // Act
            deliveryFailedState.toInTransit(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof InTransitState));
        }
    }

    @Nested
    @DisplayName("Self Transition Tests")
    class SelfTransitionTests {

        @Test
        @DisplayName("Should allow transition to same state (no-op)")
        void shouldAllowTransitionToSameState() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act
            deliveryFailedState.toDeliveryFailed(mockPackage);

            // Assert - Should complete without throwing exception
            // No interactions with package expected (no-op)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle multiple calls to toDeliveryFailed gracefully")
        void shouldHandleMultipleCallsToDeliveryFailedGracefully() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act - Multiple calls should not cause issues
            deliveryFailedState.toDeliveryFailed(mockPackage);
            deliveryFailedState.toDeliveryFailed(mockPackage);
            deliveryFailedState.toDeliveryFailed(mockPackage);

            // Assert - No interactions expected (all are no-ops)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle null package in toDeliveryFailed gracefully")
        void shouldHandleNullPackageInToDeliveryFailedGracefully() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert - Should not throw exception for no-op
            assertThatCode(() -> deliveryFailedState.toDeliveryFailed(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning to OUT_FOR_DELIVERY")
        void shouldThrowExceptionWhenTransitioningToOutForDelivery() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERED")
        void shouldThrowExceptionWhenTransitioningToDelivered() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot deliver a failed package directly");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to RETURNED")
        void shouldThrowExceptionWhenTransitioningToReturned() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot return directly from DELIVERY_FAILED");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Error Message Specificity Tests")
    class ErrorMessageSpecificityTests {

        @Test
        @DisplayName("Should have specific error message for OUT_FOR_DELIVERY transition")
        void shouldHaveSpecificErrorMessageForOutForDeliveryTransition() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");
        }

        @Test
        @DisplayName("Should have specific error message for DELIVERED transition")
        void shouldHaveSpecificErrorMessageForDeliveredTransition() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot deliver a failed package directly");
        }

        @Test
        @DisplayName("Should have specific error message for RETURNED transition")
        void shouldHaveSpecificErrorMessageForReturnedTransition() {
            // Arrange - deliveryFailedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot return directly from DELIVERY_FAILED");
        }
    }

    @Nested
    @DisplayName("Recovery State Behavior Tests")
    class RecoveryStateBehaviorTests {

        @Test
        @DisplayName("Should handle null package when transitioning to IN_TRANSIT")
        void shouldHandleNullPackageWhenTransitioningToInTransit() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveryFailedState.toInTransit(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions with null package")
        void shouldThrowExceptionImmediatelyForInvalidTransitionsWithNullPackage() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> deliveryFailedState.toDelivered(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot deliver a failed package directly");

            assertThatThrownBy(() -> deliveryFailedState.toOutForDelivery(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");

            assertThatThrownBy(() -> deliveryFailedState.toReturned(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot return directly from DELIVERY_FAILED");
        }

        @Test
        @DisplayName("Should behave as recovery state allowing only retry path")
        void shouldBehaveAsRecoveryStateAllowingOnlyRetryPath() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert - Only IN_TRANSIT should be allowed for retry
            assertThat(deliveryFailedState.canTransitionTo(PackageStatus.IN_TRANSIT))
                    .as("Should allow retry through IN_TRANSIT")
                    .isTrue();

            // All other transitions should be blocked
            PackageStatus[] blockedStatuses = {
                    PackageStatus.CREATED,
                    PackageStatus.OUT_FOR_DELIVERY,
                    PackageStatus.DELIVERED,
                    PackageStatus.DELIVERY_FAILED,
                    PackageStatus.RETURNED
            };

            for (PackageStatus status : blockedStatuses) {
                assertThat(deliveryFailedState.canTransitionTo(status))
                        .as("Should not allow direct transition to " + status)
                        .isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Behavior Verification Tests")
    class BehaviorVerificationTests {

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - deliveryFailedState already set up

            // Act
            boolean canTransitionToInTransit = deliveryFailedState.canTransitionTo(PackageStatus.IN_TRANSIT);
            boolean canTransitionToDelivered = deliveryFailedState.canTransitionTo(PackageStatus.DELIVERED);
            boolean canTransitionToReturned = deliveryFailedState.canTransitionTo(PackageStatus.RETURNED);

            // Assert
            assertThat(canTransitionToInTransit).isTrue();
            assertThat(canTransitionToDelivered).isFalse();
            assertThat(canTransitionToReturned).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert
            assertThat(deliveryFailedState.canTransitionTo(null)).isFalse();
        }

        @Test
        @DisplayName("Should maintain state immutability")
        void shouldMaintainStateImmutability() {
            // Arrange
            DeliveryFailedState state1 = new DeliveryFailedState();
            DeliveryFailedState state2 = new DeliveryFailedState();

            // Act
            state1.toDeliveryFailed(mockPackage);
            PackageStatus status1 = state1.getStatus();
            PackageStatus status2 = state2.getStatus();

            // Assert - Both instances should behave identically
            assertThat(status1).isEqualTo(status2);
            assertThat(state1.canTransitionTo(PackageStatus.IN_TRANSIT))
                    .isEqualTo(state2.canTransitionTo(PackageStatus.IN_TRANSIT));
        }

        @Test
        @DisplayName("Should have consistent behavior across multiple calls")
        void shouldHaveConsistentBehaviorAcrossMultipleCalls() {
            // Arrange - deliveryFailedState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(deliveryFailedState.getStatus()).isEqualTo(PackageStatus.DELIVERY_FAILED);
                assertThat(deliveryFailedState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
                assertThat(deliveryFailedState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();

                // Test no-op behavior consistency
                assertThatCode(() -> deliveryFailedState.toDeliveryFailed(mockPackage))
                        .doesNotThrowAnyException();
            }
        }
    }
}

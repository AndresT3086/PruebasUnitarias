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
@DisplayName("OutForDeliveryState Tests")
class OutForDeliveryStateTest {

    @Mock
    private Package mockPackage;

    private OutForDeliveryState outForDeliveryState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        outForDeliveryState = new OutForDeliveryState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return OUT_FOR_DELIVERY status")
        void shouldReturnOutForDeliveryStatus() {
            // Arrange - outForDeliveryState already set up

            // Act
            PackageStatus result = outForDeliveryState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
        }

        @Test
        @DisplayName("Should allow transitions to final states only")
        void shouldAllowTransitionsToFinalStatesOnly() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should allow transitions to final states
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.DELIVERED)).isTrue();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isTrue();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.RETURNED)).isTrue();

            // Should not allow transitions to intermediate states
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
        }
    }

    @Nested
    @DisplayName("Valid Transition Tests")
    class ValidTransitionTests {

        @Test
        @DisplayName("Should transition to DELIVERED successfully")
        void shouldTransitionToDeliveredSuccessfully() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(DeliveredState.class));

            // Act
            outForDeliveryState.toDelivered(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(DeliveredState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to DELIVERED")
        void shouldApplyCorrectStateTypeWhenTransitioningToDelivered() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(DeliveredState.class));

            // Act
            outForDeliveryState.toDelivered(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof DeliveredState));
        }

        @Test
        @DisplayName("Should transition to DELIVERY_FAILED successfully")
        void shouldTransitionToDeliveryFailedSuccessfully() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(DeliveryFailedState.class));

            // Act
            outForDeliveryState.toDeliveryFailed(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(DeliveryFailedState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to DELIVERY_FAILED")
        void shouldApplyCorrectStateTypeWhenTransitioningToDeliveryFailed() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(DeliveryFailedState.class));

            // Act
            outForDeliveryState.toDeliveryFailed(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof DeliveryFailedState));
        }

        @Test
        @DisplayName("Should transition to RETURNED successfully")
        void shouldTransitionToReturnedSuccessfully() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(ReturnedState.class));

            // Act
            outForDeliveryState.toReturned(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(ReturnedState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to RETURNED")
        void shouldApplyCorrectStateTypeWhenTransitioningToReturned() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(ReturnedState.class));

            // Act
            outForDeliveryState.toReturned(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof ReturnedState));
        }
    }

    @Nested
    @DisplayName("Self Transition Tests")
    class SelfTransitionTests {

        @Test
        @DisplayName("Should allow transition to same state (no-op)")
        void shouldAllowTransitionToSameState() {
            // Arrange - outForDeliveryState and mockPackage already set up

            // Act
            outForDeliveryState.toOutForDelivery(mockPackage);

            // Assert - Should complete without throwing exception
            // No interactions with package expected (no-op)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle multiple calls to toOutForDelivery gracefully")
        void shouldHandleMultipleCallsToOutForDeliveryGracefully() {
            // Arrange - outForDeliveryState and mockPackage already set up

            // Act - Multiple calls should not cause issues
            outForDeliveryState.toOutForDelivery(mockPackage);
            outForDeliveryState.toOutForDelivery(mockPackage);
            outForDeliveryState.toOutForDelivery(mockPackage);

            // Assert - No interactions expected (all are no-ops)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle null package in toOutForDelivery gracefully")
        void shouldHandleNullPackageInToOutForDeliveryGracefully() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should not throw exception for no-op
            assertThatCode(() -> outForDeliveryState.toOutForDelivery(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning back to IN_TRANSIT")
        void shouldThrowExceptionWhenTransitioningBackToInTransit() {
            // Arrange - outForDeliveryState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> outForDeliveryState.toInTransit(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from OUT_FOR_DELIVERY back to IN_TRANSIT");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Final Stage Delivery Tests")
    class FinalStageDeliveryTests {

        @Test
        @DisplayName("Should handle all possible delivery outcomes")
        void shouldHandleAllPossibleDeliveryOutcomes() {
            // Arrange
            doNothing().when(mockPackage).applyState(any());

            // Act & Assert - All final outcomes should be supported
            assertThatCode(() -> outForDeliveryState.toDelivered(mockPackage))
                    .doesNotThrowAnyException();

            assertThatCode(() -> outForDeliveryState.toDeliveryFailed(mockPackage))
                    .doesNotThrowAnyException();

            assertThatCode(() -> outForDeliveryState.toReturned(mockPackage))
                    .doesNotThrowAnyException();

            // Verify all transitions were attempted
            verify(mockPackage, times(3)).applyState(any());
        }

        @Test
        @DisplayName("Should represent critical delivery decision point")
        void shouldRepresentCriticalDeliveryDecisionPoint() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should allow exactly 3 possible outcomes
            PackageStatus[] finalStates = {
                    PackageStatus.DELIVERED,
                    PackageStatus.DELIVERY_FAILED,
                    PackageStatus.RETURNED
            };

            for (PackageStatus status : finalStates) {
                assertThat(outForDeliveryState.canTransitionTo(status))
                        .as("Should allow transition to final state: " + status)
                        .isTrue();
            }

            // Count allowed transitions
            PackageStatus[] allStates = PackageStatus.values();
            int allowedTransitions = 0;
            for (PackageStatus status : allStates) {
                if (outForDeliveryState.canTransitionTo(status)) {
                    allowedTransitions++;
                }
            }

            assertThat(allowedTransitions)
                    .as("Should allow exactly 3 final state transitions")
                    .isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null package for valid transitions")
        void shouldHandleNullPackageForValidTransitions() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should throw NullPointerException for valid transitions with null package
            assertThatThrownBy(() -> outForDeliveryState.toDelivered(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> outForDeliveryState.toDeliveryFailed(null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> outForDeliveryState.toReturned(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions with null package")
        void shouldThrowExceptionImmediatelyForInvalidTransitionsWithNullPackage() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> outForDeliveryState.toInTransit(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from OUT_FOR_DELIVERY back to IN_TRANSIT");
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert
            assertThat(outForDeliveryState.canTransitionTo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Behavior Verification Tests")
    class BehaviorVerificationTests {

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - outForDeliveryState already set up

            // Act
            boolean canTransitionToDelivered = outForDeliveryState.canTransitionTo(PackageStatus.DELIVERED);
            boolean canTransitionToFailed = outForDeliveryState.canTransitionTo(PackageStatus.DELIVERY_FAILED);
            boolean canTransitionToReturned = outForDeliveryState.canTransitionTo(PackageStatus.RETURNED);
            boolean canTransitionToInTransit = outForDeliveryState.canTransitionTo(PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(canTransitionToDelivered).isTrue();
            assertThat(canTransitionToFailed).isTrue();
            assertThat(canTransitionToReturned).isTrue();
            assertThat(canTransitionToInTransit).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should maintain state immutability")
        void shouldMaintainStateImmutability() {
            // Arrange
            OutForDeliveryState state1 = new OutForDeliveryState();
            OutForDeliveryState state2 = new OutForDeliveryState();

            // Act
            state1.toOutForDelivery(mockPackage);
            PackageStatus status1 = state1.getStatus();
            PackageStatus status2 = state2.getStatus();

            // Assert - Both instances should behave identically
            assertThat(status1).isEqualTo(status2);
            assertThat(state1.canTransitionTo(PackageStatus.DELIVERED))
                    .isEqualTo(state2.canTransitionTo(PackageStatus.DELIVERED));
        }

        @Test
        @DisplayName("Should have consistent behavior across multiple calls")
        void shouldHaveConsistentBehaviorAcrossMultipleCalls() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(outForDeliveryState.getStatus()).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
                assertThat(outForDeliveryState.canTransitionTo(PackageStatus.DELIVERED)).isTrue();
                assertThat(outForDeliveryState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isTrue();
                assertThat(outForDeliveryState.canTransitionTo(PackageStatus.RETURNED)).isTrue();
                assertThat(outForDeliveryState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();

                // Test no-op behavior consistency
                assertThatCode(() -> outForDeliveryState.toOutForDelivery(mockPackage))
                        .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Should prevent backward progression in workflow")
        void shouldPreventBackwardProgressionInWorkflow() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - Should not allow going backwards in the workflow
            assertThatThrownBy(() -> outForDeliveryState.toInTransit(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from OUT_FOR_DELIVERY back to IN_TRANSIT");

            // Should not allow transition capability check for backward states
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
        }

        @Test
        @DisplayName("Should enforce one-way progression to final states")
        void shouldEnforceOneWayProgressionToFinalStates() {
            // Arrange - outForDeliveryState already set up

            // Act & Assert - This should be a critical decision point in delivery
            // All allowed transitions should lead to final states
            PackageStatus[] allowedTransitions = {
                    PackageStatus.DELIVERED,
                    PackageStatus.DELIVERY_FAILED,
                    PackageStatus.RETURNED
            };

            for (PackageStatus status : allowedTransitions) {
                assertThat(outForDeliveryState.canTransitionTo(status))
                        .as("Should allow transition to final state: " + status)
                        .isTrue();
            }

            // No intermediate states should be allowed
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
            assertThat(outForDeliveryState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
        }
    }
}

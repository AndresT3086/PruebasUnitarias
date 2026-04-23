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
@DisplayName("DeliveredState Tests")
class DeliveredStateTest {

    @Mock
    private Package mockPackage;

    private DeliveredState deliveredState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        deliveredState = new DeliveredState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return DELIVERED status")
        void shouldReturnDeliveredStatus() {
            // Arrange - deliveredState already set up

            // Act
            PackageStatus result = deliveredState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.DELIVERED);
        }

        @Test
        @DisplayName("Should not allow any status transitions")
        void shouldNotAllowAnyStatusTransitions() {
            // Arrange - deliveredState already set up

            // Act & Assert - No transitions should be allowed from DELIVERED state
            assertThat(deliveredState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(deliveredState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
            assertThat(deliveredState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
            assertThat(deliveredState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            assertThat(deliveredState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();
            assertThat(deliveredState.canTransitionTo(PackageStatus.RETURNED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Self Transition Tests")
    class SelfTransitionTests {

        @Test
        @DisplayName("Should allow transition to same state (no-op)")
        void shouldAllowTransitionToSameState() {
            // Arrange - deliveredState and mockPackage already set up

            // Act
            deliveredState.toDelivered(mockPackage);

            // Assert - Should complete without throwing exception
            // No interactions with package expected (no-op)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle multiple calls to toDelivered gracefully")
        void shouldHandleMultipleCallsToDeliveredGracefully() {
            // Arrange - deliveredState and mockPackage already set up

            // Act - Multiple calls should not cause issues
            deliveredState.toDelivered(mockPackage);
            deliveredState.toDelivered(mockPackage);
            deliveredState.toDelivered(mockPackage);

            // Assert - No interactions expected (all are no-ops)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle null package in toDelivered gracefully")
        void shouldHandleNullPackageInToDeliveredGracefully() {
            // Arrange - deliveredState already set up

            // Act & Assert - Should not throw exception for no-op
            assertThatCode(() -> deliveredState.toDelivered(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning to IN_TRANSIT")
        void shouldThrowExceptionWhenTransitioningToInTransit() {
            // Arrange - deliveredState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveredState.toInTransit(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to OUT_FOR_DELIVERY")
        void shouldThrowExceptionWhenTransitioningToOutForDelivery() {
            // Arrange - deliveredState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveredState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERY_FAILED")
        void shouldThrowExceptionWhenTransitioningToDeliveryFailed() {
            // Arrange - deliveredState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveredState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to RETURNED")
        void shouldThrowExceptionWhenTransitioningToReturned() {
            // Arrange - deliveredState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> deliveredState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw consistent exception message for all invalid transitions")
        void shouldThrowConsistentExceptionMessageForAllInvalidTransitions() {
            // Arrange
            String expectedMessage = "Package has been delivered, cannot change status";

            // Act & Assert - All invalid transitions should have the same message
            assertThatThrownBy(() -> deliveredState.toInTransit(mockPackage))
                    .hasMessage(expectedMessage);

            assertThatThrownBy(() -> deliveredState.toOutForDelivery(mockPackage))
                    .hasMessage(expectedMessage);

            assertThatThrownBy(() -> deliveredState.toDeliveryFailed(mockPackage))
                    .hasMessage(expectedMessage);

            assertThatThrownBy(() -> deliveredState.toReturned(mockPackage))
                    .hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions with null package")
        void shouldThrowExceptionImmediatelyForInvalidTransitionsWithNullPackage() {
            // Arrange - deliveredState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> deliveredState.toInTransit(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            assertThatThrownBy(() -> deliveredState.toOutForDelivery(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            assertThatThrownBy(() -> deliveredState.toDeliveryFailed(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");

            assertThatThrownBy(() -> deliveredState.toReturned(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Package has been delivered, cannot change status");
        }
    }

    @Nested
    @DisplayName("Behavior Verification Tests")
    class BehaviorVerificationTests {

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - deliveredState already set up

            // Act
            boolean canTransitionToInTransit = deliveredState.canTransitionTo(PackageStatus.IN_TRANSIT);
            boolean canTransitionToDelivered = deliveredState.canTransitionTo(PackageStatus.DELIVERED);
            boolean canTransitionToReturned = deliveredState.canTransitionTo(PackageStatus.RETURNED);

            // Assert
            assertThat(canTransitionToInTransit).isFalse();
            assertThat(canTransitionToDelivered).isFalse();
            assertThat(canTransitionToReturned).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - deliveredState already set up

            // Act & Assert
            assertThat(deliveredState.canTransitionTo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Final State Behavior Tests")
    class FinalStateBehaviorTests {

        @Test
        @DisplayName("Should behave as final state - no transitions allowed")
        void shouldBehaveAsFinalStateNoTransitionsAllowed() {
            // Arrange - deliveredState already set up

            // Act & Assert - Verify this is truly a final state
            PackageStatus[] allStatuses = PackageStatus.values();

            for (PackageStatus status : allStatuses) {
                assertThat(deliveredState.canTransitionTo(status))
                        .as("Should not allow transition to " + status)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("Should maintain immutability across operations")
        void shouldMaintainImmutabilityAcrossOperations() {
            // Arrange
            DeliveredState state1 = new DeliveredState();
            DeliveredState state2 = new DeliveredState();

            // Act
            state1.toDelivered(mockPackage);
            PackageStatus status1 = state1.getStatus();
            PackageStatus status2 = state2.getStatus();

            // Assert - Both instances should behave identically
            assertThat(status1).isEqualTo(status2);
            assertThat(status1).isEqualTo(PackageStatus.DELIVERED);
        }

        @Test
        @DisplayName("Should have consistent behavior across multiple calls")
        void shouldHaveConsistentBehaviorAcrossMultipleCalls() {
            // Arrange - deliveredState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(deliveredState.getStatus()).isEqualTo(PackageStatus.DELIVERED);

                // Test a few status checks for consistency
                assertThat(deliveredState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
                assertThat(deliveredState.canTransitionTo(PackageStatus.RETURNED)).isFalse();

                // Test no-op behavior consistency
                assertThatCode(() -> deliveredState.toDelivered(mockPackage))
                        .doesNotThrowAnyException();
            }
        }
    }
}

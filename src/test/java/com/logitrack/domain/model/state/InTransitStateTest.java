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
@DisplayName("InTransitState Tests")
class InTransitStateTest {

    @Mock
    private Package mockPackage;

    private InTransitState inTransitState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        inTransitState = new InTransitState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return IN_TRANSIT status")
        void shouldReturnInTransitStatus() {
            // Arrange - inTransitState already set up

            // Act
            PackageStatus result = inTransitState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("Should only allow transition to OUT_FOR_DELIVERY")
        void shouldOnlyAllowTransitionToOutForDelivery() {
            // Arrange - inTransitState already set up

            // Act & Assert
            assertThat(inTransitState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isTrue();
            assertThat(inTransitState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(inTransitState.canTransitionTo(PackageStatus.IN_TRANSIT)).isFalse();
            assertThat(inTransitState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            assertThat(inTransitState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();
            assertThat(inTransitState.canTransitionTo(PackageStatus.RETURNED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Valid Transition Tests")
    class ValidTransitionTests {

        @Test
        @DisplayName("Should transition to OUT_FOR_DELIVERY successfully")
        void shouldTransitionToOutForDeliverySuccessfully() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(OutForDeliveryState.class));

            // Act
            inTransitState.toOutForDelivery(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(OutForDeliveryState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to OUT_FOR_DELIVERY")
        void shouldApplyCorrectStateTypeWhenTransitioningToOutForDelivery() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(OutForDeliveryState.class));

            // Act
            inTransitState.toOutForDelivery(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof OutForDeliveryState));
        }
    }

    @Nested
    @DisplayName("Self Transition Tests")
    class SelfTransitionTests {

        @Test
        @DisplayName("Should allow transition to same state (no-op)")
        void shouldAllowTransitionToSameState() {
            // Arrange - inTransitState and mockPackage already set up

            // Act
            inTransitState.toInTransit(mockPackage);

            // Assert - Should complete without throwing exception
            // No interactions with package expected (no-op)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle multiple calls to toInTransit gracefully")
        void shouldHandleMultipleCallsToInTransitGracefully() {
            // Arrange - inTransitState and mockPackage already set up

            // Act - Multiple calls should not cause issues
            inTransitState.toInTransit(mockPackage);
            inTransitState.toInTransit(mockPackage);
            inTransitState.toInTransit(mockPackage);

            // Assert - No interactions expected (all are no-ops)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle null package in toInTransit gracefully")
        void shouldHandleNullPackageInToInTransitGracefully() {
            // Arrange - inTransitState already set up

            // Act & Assert - Should not throw exception for no-op
            assertThatCode(() -> inTransitState.toInTransit(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERED")
        void shouldThrowExceptionWhenTransitioningToDelivered() {
            // Arrange - inTransitState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> inTransitState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERED directly");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERY_FAILED")
        void shouldThrowExceptionWhenTransitioningToDeliveryFailed() {
            // Arrange - inTransitState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> inTransitState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERY_FAILED");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to RETURNED")
        void shouldThrowExceptionWhenTransitioningToReturned() {
            // Arrange - inTransitState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> inTransitState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to RETURNED directly");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should enforce sequential delivery workflow")
        void shouldEnforceSequentialDeliveryWorkflow() {
            // Arrange - inTransitState already set up

            // Act & Assert - Should only allow progression to next step in workflow
            assertThat(inTransitState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY))
                    .as("Should allow progression to OUT_FOR_DELIVERY")
                    .isTrue();

            // Should not allow skipping steps
            assertThat(inTransitState.canTransitionTo(PackageStatus.DELIVERED))
                    .as("Should not allow skipping to DELIVERED")
                    .isFalse();

            assertThat(inTransitState.canTransitionTo(PackageStatus.DELIVERY_FAILED))
                    .as("Should not allow direct failure from transit")
                    .isFalse();

            assertThat(inTransitState.canTransitionTo(PackageStatus.RETURNED))
                    .as("Should not allow direct return from transit")
                    .isFalse();
        }

        @Test
        @DisplayName("Should have specific error messages for business rule violations")
        void shouldHaveSpecificErrorMessagesForBusinessRuleViolations() {
            // Arrange - inTransitState and mockPackage already set up

            // Act & Assert - Test specific error messages
            assertThatThrownBy(() -> inTransitState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERED directly");

            assertThatThrownBy(() -> inTransitState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERY_FAILED");

            assertThatThrownBy(() -> inTransitState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to RETURNED directly");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null package when transitioning to OUT_FOR_DELIVERY")
        void shouldHandleNullPackageWhenTransitioningToOutForDelivery() {
            // Arrange - inTransitState already set up

            // Act & Assert
            assertThatThrownBy(() -> inTransitState.toOutForDelivery(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions with null package")
        void shouldThrowExceptionImmediatelyForInvalidTransitionsWithNullPackage() {
            // Arrange - inTransitState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> inTransitState.toDelivered(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERED directly");

            assertThatThrownBy(() -> inTransitState.toDeliveryFailed(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to DELIVERY_FAILED");

            assertThatThrownBy(() -> inTransitState.toReturned(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from IN_TRANSIT to RETURNED directly");
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - inTransitState already set up

            // Act & Assert
            assertThat(inTransitState.canTransitionTo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Workflow State Behavior Tests")
    class WorkflowStateBehaviorTests {

        @Test
        @DisplayName("Should behave as intermediate workflow state")
        void shouldBehaveAsIntermediateWorkflowState() {
            // Arrange - inTransitState already set up

            // Act & Assert - Should allow exactly one forward progression
            PackageStatus[] allStatuses = PackageStatus.values();
            int allowedTransitions = 0;

            for (PackageStatus status : allStatuses) {
                if (inTransitState.canTransitionTo(status)) {
                    allowedTransitions++;
                    assertThat(status).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
                }
            }

            assertThat(allowedTransitions)
                    .as("Should allow exactly one forward transition")
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - inTransitState already set up

            // Act
            boolean canTransitionToOutForDelivery = inTransitState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY);
            boolean canTransitionToDelivered = inTransitState.canTransitionTo(PackageStatus.DELIVERED);
            boolean canTransitionToReturned = inTransitState.canTransitionTo(PackageStatus.RETURNED);

            // Assert
            assertThat(canTransitionToOutForDelivery).isTrue();
            assertThat(canTransitionToDelivered).isFalse();
            assertThat(canTransitionToReturned).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should maintain state immutability")
        void shouldMaintainStateImmutability() {
            // Arrange
            InTransitState state1 = new InTransitState();
            InTransitState state2 = new InTransitState();

            // Act
            state1.toInTransit(mockPackage);
            PackageStatus status1 = state1.getStatus();
            PackageStatus status2 = state2.getStatus();

            // Assert - Both instances should behave identically
            assertThat(status1).isEqualTo(status2);
            assertThat(state1.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY))
                    .isEqualTo(state2.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY));
        }

        @Test
        @DisplayName("Should have consistent behavior across multiple calls")
        void shouldHaveConsistentBehaviorAcrossMultipleCalls() {
            // Arrange - inTransitState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(inTransitState.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
                assertThat(inTransitState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isTrue();
                assertThat(inTransitState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();

                // Test no-op behavior consistency
                assertThatCode(() -> inTransitState.toInTransit(mockPackage))
                        .doesNotThrowAnyException();
            }
        }
    }
}

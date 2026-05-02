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
@DisplayName("ReturnedState Tests")
class ReturnedStateTest {

    @Mock
    private Package mockPackage;

    private ReturnedState returnedState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        returnedState = new ReturnedState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return RETURNED status")
        void shouldReturnReturnedStatus() {
            // Arrange - returnedState already set up

            // Act
            PackageStatus result = returnedState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.RETURNED);
        }

        @Test
        @DisplayName("Should only allow transition to IN_TRANSIT")
        void shouldOnlyAllowTransitionToInTransit() {
            // Arrange - returnedState already set up

            // Act & Assert
            assertThat(returnedState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
            assertThat(returnedState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(returnedState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
            assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();
            assertThat(returnedState.canTransitionTo(PackageStatus.RETURNED)).isFalse();
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
            returnedState.toInTransit(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(InTransitState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to IN_TRANSIT")
        void shouldApplyCorrectStateTypeWhenTransitioningToInTransit() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(InTransitState.class));

            // Act
            returnedState.toInTransit(mockPackage);

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
            // Arrange - returnedState and mockPackage already set up

            // Act
            returnedState.toReturned(mockPackage);

            // Assert - Should complete without throwing exception
            // No interactions with package expected (no-op)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle multiple calls to toReturned gracefully")
        void shouldHandleMultipleCallsToReturnedGracefully() {
            // Arrange - returnedState and mockPackage already set up

            // Act - Multiple calls should not cause issues
            returnedState.toReturned(mockPackage);
            returnedState.toReturned(mockPackage);
            returnedState.toReturned(mockPackage);

            // Assert - No interactions expected (all are no-ops)
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should handle null package in toReturned gracefully")
        void shouldHandleNullPackageInToReturnedGracefully() {
            // Arrange - returnedState already set up

            // Act & Assert - Should not throw exception for no-op
            assertThatCode(() -> returnedState.toReturned(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning to OUT_FOR_DELIVERY")
        void shouldThrowExceptionWhenTransitioningToOutForDelivery() {
            // Arrange - returnedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> returnedState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERED")
        void shouldThrowExceptionWhenTransitioningToDelivered() {
            // Arrange - returnedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> returnedState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot be delivered");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERY_FAILED")
        void shouldThrowExceptionWhenTransitioningToDeliveryFailed() {
            // Arrange - returnedState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> returnedState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot fail delivery");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Recovery State Logic Tests")
    class RecoveryStateLogicTests {

        @Test
        @DisplayName("Should enforce proper workflow for redelivery attempt")
        void shouldEnforceProperWorkflowForRedeliveryAttempt() {
            // Arrange - returnedState already set up

            // Act & Assert - Should only allow restart through proper workflow
            assertThat(returnedState.canTransitionTo(PackageStatus.IN_TRANSIT))
                    .as("Should allow redelivery attempt through IN_TRANSIT")
                    .isTrue();

            // Should not allow skipping workflow steps
            assertThat(returnedState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY))
                    .as("Should not allow skipping to OUT_FOR_DELIVERY")
                    .isFalse();

            assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERED))
                    .as("Should not allow direct delivery of returned package")
                    .isFalse();
        }

        @Test
        @DisplayName("Should have specific error messages for logical inconsistencies")
        void shouldHaveSpecificErrorMessagesForLogicalInconsistencies() {
            // Arrange - returnedState and mockPackage already set up

            // Act & Assert - Test specific error messages for business rule violations
            assertThatThrownBy(() -> returnedState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot be delivered");

            assertThatThrownBy(() -> returnedState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot fail delivery");

            assertThatThrownBy(() -> returnedState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");
        }

        @Test
        @DisplayName("Should behave as recovery state allowing only proper restart")
        void shouldBehaveAsRecoveryStateAllowingOnlyProperRestart() {
            // Arrange - returnedState already set up

            // Act & Assert - Should allow exactly one restart path
            PackageStatus[] allStatuses = PackageStatus.values();
            int allowedTransitions = 0;

            for (PackageStatus status : allStatuses) {
                if (returnedState.canTransitionTo(status)) {
                    allowedTransitions++;
                    assertThat(status).isEqualTo(PackageStatus.IN_TRANSIT);
                }
            }

            assertThat(allowedTransitions)
                    .as("Should allow exactly one restart transition")
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null package when transitioning to IN_TRANSIT")
        void shouldHandleNullPackageWhenTransitioningToInTransit() {
            // Arrange - returnedState already set up

            // Act & Assert
            assertThatThrownBy(() -> returnedState.toInTransit(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions with null package")
        void shouldThrowExceptionImmediatelyForInvalidTransitionsWithNullPackage() {
            // Arrange - returnedState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> returnedState.toDelivered(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot be delivered");

            assertThatThrownBy(() -> returnedState.toDeliveryFailed(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Returned package cannot fail delivery");

            assertThatThrownBy(() -> returnedState.toOutForDelivery(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Must go through IN_TRANSIT before OUT_FOR_DELIVERY");
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - returnedState already set up

            // Act & Assert
            assertThat(returnedState.canTransitionTo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Business Logic Validation Tests")
    class BusinessLogicValidationTests {

        @Test
        @DisplayName("Should prevent direct transitions to delivery outcomes")
        void shouldPreventDirectTransitionsToDeliveryOutcomes() {
            // Arrange - returnedState already set up

            // Act & Assert - Returned packages should not directly transition to delivery outcomes
            assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERED))
                    .as("Returned package should not be directly deliverable")
                    .isFalse();

            assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERY_FAILED))
                    .as("Returned package cannot fail delivery")
                    .isFalse();

            // Test the actual transition attempts
            assertThatThrownBy(() -> returnedState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);

            assertThatThrownBy(() -> returnedState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("Should enforce redelivery workflow integrity")
        void shouldEnforceRedeliveryWorkflowIntegrity() {
            // Arrange - returnedState already set up

            // Act & Assert - Should require going through proper workflow for redelivery
            // Only IN_TRANSIT should be allowed for restart
            assertThat(returnedState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();

            // Should not allow skipping workflow steps
            assertThat(returnedState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
            assertThat(returnedState.canTransitionTo(PackageStatus.CREATED)).isFalse();
        }
    }

    @Nested
    @DisplayName("Behavior Verification Tests")
    class BehaviorVerificationTests {

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - returnedState already set up

            // Act
            boolean canTransitionToInTransit = returnedState.canTransitionTo(PackageStatus.IN_TRANSIT);
            boolean canTransitionToDelivered = returnedState.canTransitionTo(PackageStatus.DELIVERED);
            boolean canTransitionToFailed = returnedState.canTransitionTo(PackageStatus.DELIVERY_FAILED);

            // Assert
            assertThat(canTransitionToInTransit).isTrue();
            assertThat(canTransitionToDelivered).isFalse();
            assertThat(canTransitionToFailed).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should maintain state immutability")
        void shouldMaintainStateImmutability() {
            // Arrange
            ReturnedState state1 = new ReturnedState();
            ReturnedState state2 = new ReturnedState();

            // Act
            state1.toReturned(mockPackage);
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
            // Arrange - returnedState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(returnedState.getStatus()).isEqualTo(PackageStatus.RETURNED);
                assertThat(returnedState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
                assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
                assertThat(returnedState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();

                // Test no-op behavior consistency
                assertThatCode(() -> returnedState.toReturned(mockPackage))
                        .doesNotThrowAnyException();
            }
        }
    }
}

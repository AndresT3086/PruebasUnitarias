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
@DisplayName("CreatedState Tests")
class CreatedStateTest {

    @Mock
    private Package mockPackage;

    private CreatedState createdState;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        createdState = new CreatedState();
    }

    @Nested
    @DisplayName("State Information Tests")
    class StateInformationTests {

        @Test
        @DisplayName("Should return CREATED status")
        void shouldReturnCreatedStatus() {
            // Arrange - createdState already set up

            // Act
            PackageStatus result = createdState.getStatus();

            // Assert
            assertThat(result).isEqualTo(PackageStatus.CREATED);
        }

        @Test
        @DisplayName("Should allow transition to IN_TRANSIT only")
        void shouldAllowTransitionToInTransitOnly() {
            // Arrange - createdState already set up

            // Act & Assert
            assertThat(createdState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
            assertThat(createdState.canTransitionTo(PackageStatus.CREATED)).isFalse();
            assertThat(createdState.canTransitionTo(PackageStatus.OUT_FOR_DELIVERY)).isFalse();
            assertThat(createdState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            assertThat(createdState.canTransitionTo(PackageStatus.DELIVERY_FAILED)).isFalse();
            assertThat(createdState.canTransitionTo(PackageStatus.RETURNED)).isFalse();
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
            createdState.toInTransit(mockPackage);

            // Assert
            verify(mockPackage, times(1)).applyState(any(InTransitState.class));
        }

        @Test
        @DisplayName("Should apply correct state type when transitioning to IN_TRANSIT")
        void shouldApplyCorrectStateTypeWhenTransitioningToInTransit() {
            // Arrange
            doNothing().when(mockPackage).applyState(any(InTransitState.class));

            // Act
            createdState.toInTransit(mockPackage);

            // Assert
            verify(mockPackage).applyState(argThat(state -> state instanceof InTransitState));
        }
    }

    @Nested
    @DisplayName("Invalid Transition Tests")
    class InvalidTransitionTests {

        @Test
        @DisplayName("Should throw exception when transitioning to OUT_FOR_DELIVERY")
        void shouldThrowExceptionWhenTransitioningToOutForDelivery() {
            // Arrange - createdState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> createdState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to OUT_FOR_DELIVERY directly");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERED")
        void shouldThrowExceptionWhenTransitioningToDelivered() {
            // Arrange - createdState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> createdState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to DELIVERED directly");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to DELIVERY_FAILED")
        void shouldThrowExceptionWhenTransitioningToDeliveryFailed() {
            // Arrange - createdState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> createdState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to DELIVERY_FAILED");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }

        @Test
        @DisplayName("Should throw exception when transitioning to RETURNED")
        void shouldThrowExceptionWhenTransitioningToReturned() {
            // Arrange - createdState and mockPackage already set up

            // Act & Assert
            assertThatThrownBy(() -> createdState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to RETURNED");

            // Verify no state change was attempted
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Behavior Verification Tests")
    class BehaviorVerificationTests {

        @Test
        @DisplayName("Should not interact with package when checking transition capability")
        void shouldNotInteractWithPackageWhenCheckingTransitionCapability() {
            // Arrange - createdState already set up

            // Act
            boolean canTransitionToInTransit = createdState.canTransitionTo(PackageStatus.IN_TRANSIT);
            boolean canTransitionToDelivered = createdState.canTransitionTo(PackageStatus.DELIVERED);

            // Assert
            assertThat(canTransitionToInTransit).isTrue();
            assertThat(canTransitionToDelivered).isFalse();

            // Verify no interactions with mock package
            verifyNoInteractions(mockPackage);
        }

        @Test
        @DisplayName("Should throw exception immediately for invalid transitions")
        void shouldThrowExceptionImmediatelyForInvalidTransitions() {
            // Arrange - createdState and mockPackage already set up

            // Act & Assert - Test all invalid transitions
            assertThatThrownBy(() -> createdState.toOutForDelivery(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);

            assertThatThrownBy(() -> createdState.toDelivered(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);

            assertThatThrownBy(() -> createdState.toDeliveryFailed(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);

            assertThatThrownBy(() -> createdState.toReturned(mockPackage))
                    .isInstanceOf(InvalidStateTransitionException.class);

            // Verify package was never modified
            verify(mockPackage, never()).applyState(any());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null package gracefully when transitioning to IN_TRANSIT")
        void shouldHandleNullPackageGracefullyWhenTransitioningToInTransit() {
            // Arrange - createdState already set up

            // Act & Assert
            assertThatThrownBy(() -> createdState.toInTransit(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle null package gracefully when transitioning to invalid states")
        void shouldHandleNullPackageGracefullyWhenTransitioningToInvalidStates() {
            // Arrange - createdState already set up

            // Act & Assert - Should throw InvalidStateTransitionException before NullPointerException
            assertThatThrownBy(() -> createdState.toDelivered(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to DELIVERED directly");

            assertThatThrownBy(() -> createdState.toOutForDelivery(null))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessage("Cannot transition from CREATED to OUT_FOR_DELIVERY directly");
        }

        @Test
        @DisplayName("Should check transition capability for null status")
        void shouldCheckTransitionCapabilityForNullStatus() {
            // Arrange - createdState already set up

            // Act & Assert
            assertThat(createdState.canTransitionTo(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("State Pattern Compliance Tests")
    class StatePatternComplianceTests {

        @Test
        @DisplayName("Should maintain state immutability")
        void shouldMaintainStateImmutability() {
            // Arrange
            CreatedState state1 = new CreatedState();
            CreatedState state2 = new CreatedState();

            // Act
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
            // Arrange - createdState already set up

            // Act & Assert - Multiple calls should yield same results
            for (int i = 0; i < 5; i++) {
                assertThat(createdState.getStatus()).isEqualTo(PackageStatus.CREATED);
                assertThat(createdState.canTransitionTo(PackageStatus.IN_TRANSIT)).isTrue();
                assertThat(createdState.canTransitionTo(PackageStatus.DELIVERED)).isFalse();
            }
        }
    }
}

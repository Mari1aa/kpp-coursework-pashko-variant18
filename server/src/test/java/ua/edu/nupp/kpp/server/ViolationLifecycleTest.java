package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViolationLifecycleTest {

    @Test
    void detectedCanBeVerified() {
        assertTrue(ViolationService.isAllowedTransition(ViolationState.DETECTED, ViolationState.VERIFIED));
    }

    @Test
    void verifiedCanGetActionPlanned() {
        assertTrue(ViolationService.isAllowedTransition(ViolationState.VERIFIED, ViolationState.ACTION_PLANNED));
    }

    @Test
    void actionPlannedCanBeClosed() {
        assertTrue(ViolationService.isAllowedTransition(ViolationState.ACTION_PLANNED, ViolationState.CLOSED));
    }

    @Test
    void skippingAStateIsForbidden() {
        assertFalse(ViolationService.isAllowedTransition(ViolationState.DETECTED, ViolationState.ACTION_PLANNED));
        assertFalse(ViolationService.isAllowedTransition(ViolationState.DETECTED, ViolationState.CLOSED));
        assertFalse(ViolationService.isAllowedTransition(ViolationState.VERIFIED, ViolationState.CLOSED));
    }

    @Test
    void movingBackwardIsForbidden() {
        assertFalse(ViolationService.isAllowedTransition(ViolationState.VERIFIED, ViolationState.DETECTED));
        assertFalse(ViolationService.isAllowedTransition(ViolationState.CLOSED, ViolationState.ACTION_PLANNED));
    }

    @Test
    void closedViolationCannotChangeState() {
        for (ViolationState next : ViolationState.values()) {
            assertFalse(ViolationService.isAllowedTransition(ViolationState.CLOSED, next));
        }
    }

    @Test
    void stayingInTheSameStateIsForbidden() {
        for (ViolationState state : ViolationState.values()) {
            assertFalse(ViolationService.isAllowedTransition(state, state));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "DETECTED, VERIFIED, true",
            "VERIFIED, ACTION_PLANNED, true",
            "ACTION_PLANNED, CLOSED, true",
            "DETECTED, CLOSED, false",
            "CLOSED, DETECTED, false"
    })
    void transitionTable(ViolationState from, ViolationState to, boolean allowed) {
        org.junit.jupiter.api.Assertions.assertEquals(allowed, ViolationService.isAllowedTransition(from, to));
    }
}

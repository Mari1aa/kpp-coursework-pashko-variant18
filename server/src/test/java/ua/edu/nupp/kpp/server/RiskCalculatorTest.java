package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskCalculatorTest {

    private static final double DELTA = 0.001;

    /** Повний розрахунок так само, як у ViolationService: площа, тяжкість, чутливість -> індекс. */
    private static double index(double areaHa, int severity, int sensitivity) {
        double f1 = RiskCalculator.factor1(areaHa);
        double f2 = RiskCalculator.factor2(severity);
        double f3 = RiskCalculator.factor3(sensitivity);
        return RiskCalculator.calculate(f1, f2, f3);
    }

    @Test
    void factor1IsTenPointsPerHectare() {
        assertEquals(25.0, RiskCalculator.factor1(2.5), DELTA);
        assertEquals(4.0, RiskCalculator.factor1(0.4), DELTA);
    }

    @Test
    void factor1IsCappedAtHundred() {
        assertEquals(100.0, RiskCalculator.factor1(12.0), DELTA);
        assertEquals(100.0, RiskCalculator.factor1(10.0), DELTA);
    }

    @Test
    void factor2IsTwentyPointsPerSeverityLevel() {
        assertEquals(20.0, RiskCalculator.factor2(1), DELTA);
        assertEquals(80.0, RiskCalculator.factor2(4), DELTA);
        assertEquals(100.0, RiskCalculator.factor2(5), DELTA);
    }

    @Test
    void factor3IsTwentyPointsPerSensitivityLevel() {
        assertEquals(20.0, RiskCalculator.factor3(1), DELTA);
        assertEquals(60.0, RiskCalculator.factor3(3), DELTA);
        assertEquals(100.0, RiskCalculator.factor3(5), DELTA);
    }

    @Test
    void controlExampleOne() {
        assertEquals(58.0, index(2.5, 4, 4), DELTA);
    }

    @Test
    void controlExampleTwo() {
        assertEquals(40.6, index(0.4, 2, 5), DELTA);
    }

    @Test
    void controlExampleWithCappedArea() {
        assertEquals(95.0, index(12.0, 5, 4), DELTA);
    }

    @Test
    void controlExampleThree() {
        assertEquals(55.0, index(3.0, 4, 3), DELTA);
        assertEquals(48.2, index(1.8, 3, 4), DELTA);
    }

    @Test
    void weightsAreFortyThirtyFiveTwentyFive() {
        assertEquals(40.0, RiskCalculator.calculate(100, 0, 0), DELTA);
        assertEquals(35.0, RiskCalculator.calculate(0, 100, 0), DELTA);
        assertEquals(25.0, RiskCalculator.calculate(0, 0, 100), DELTA);
        assertEquals(100.0, RiskCalculator.calculate(100, 100, 100), DELTA);
    }

    @Test
    void resultIsRoundedToTwoDecimals() {
        assertEquals(18.54, RiskCalculator.calculate(10.1, 20, 30), DELTA);
    }

    @Test
    void factorAboveHundredIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(100.1, 50, 50));
    }

    @Test
    void negativeFactorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(50, -1, 50));
    }
}

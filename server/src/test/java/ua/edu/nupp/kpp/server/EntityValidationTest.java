package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityValidationTest {

    private static ForestPlot plot(String code, String name, String type, double area, int sensitivity) {
        return new ForestPlot(null, code, name, type, area, sensitivity);
    }

    private static Observation observation(String date, String observer, String category,
                                           double area, String description) {
        return new Observation(null, 1L, date, observer, category, area, description);
    }

    @Test
    void correctPlotPassesValidation() {
        assertDoesNotThrow(() -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 12.5, 4)));
    }

    @Test
    void plotWithEmptyCodeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.validate(plot("  ", "Ділянка 1", "Сосновий", 12.5, 4)));
    }

    @Test
    void plotWithNonPositiveAreaIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 0, 4)));
        assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", -3, 4)));
    }

    @Test
    void plotSensitivityMustBeFromOneToFive() {
        assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 5, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 5, 6)));
        assertDoesNotThrow(() -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 5, 1)));
        assertDoesNotThrow(() -> ForestPlotService.validate(plot("LP-01", "Ділянка 1", "Сосновий", 5, 5)));
    }

    @Test
    void requireRejectsNullAndBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> ForestPlotService.require(null, "Поле"));
        assertThrows(IllegalArgumentException.class, () -> ForestPlotService.require("   ", "Поле"));
        assertDoesNotThrow(() -> ForestPlotService.require("значення", "Поле"));
    }

    @Test
    void requireMessageNamesTheField() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ForestPlotService.require("", "Код ділянки"));
        assertEquals("Код ділянки є обов'язковим", e.getMessage());
    }

    @Test
    void correctObservationPassesValidation() {
        assertDoesNotThrow(() -> ObservationService.validateFields(
                observation("2026-10-05", "Іваненко", "Незаконна рубка", 2.5, "Опис")));
    }

    @Test
    void observationWithWrongDateIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ObservationService.validateFields(
                observation("05.10.2026", "Іваненко", "Незаконна рубка", 2.5, "")));
    }

    @Test
    void observationWithNegativeAreaIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ObservationService.validateFields(
                observation("2026-10-05", "Іваненко", "Незаконна рубка", -0.1, "")));
    }

    @Test
    void observationWithTooLongDescriptionIsRejected() {
        String longText = "а".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> ObservationService.validateFields(
                observation("2026-10-05", "Іваненко", "Незаконна рубка", 1, longText)));
        assertDoesNotThrow(() -> ObservationService.validateFields(
                observation("2026-10-05", "Іваненко", "Незаконна рубка", 1, "а".repeat(500))));
    }

    @Test
    void observationRequiresObserverAndCategory() {
        assertThrows(IllegalArgumentException.class, () -> ObservationService.validateFields(
                observation("2026-10-05", "", "Незаконна рубка", 1, "")));
        assertThrows(IllegalArgumentException.class, () -> ObservationService.validateFields(
                observation("2026-10-05", "Іваненко", " ", 1, "")));
    }
}

package ua.edu.nupp.kpp.client;

public enum ViolationState {
    DETECTED("Виявлено"),
    VERIFIED("Перевірено"),
    ACTION_PLANNED("Захід заплановано"),
    CLOSED("Закрито");

    private final String label;

    ViolationState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

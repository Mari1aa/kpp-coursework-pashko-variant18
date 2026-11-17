package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainApp extends Application {

    private interface UiAction {
        void run() throws Exception;
    }

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());
    private static final Path CSV_PATH = Path.of("sample-data", "observations.csv");

    private final HttpForestClient api = new HttpForestClient();

    private final TableView<ForestPlot> plotTable = new TableView<>();
    private final TableView<Observation> observationTable = new TableView<>();
    private final TableView<Violation> violationTable = new TableView<>();
    private final TableView<InspectionTask> taskTable = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Лісові ділянки", plotPane()),
                new Tab("Спостереження", observationPane()),
                new Tab("Порушення", violationPane()),
                new Tab("Інспекційні завдання", taskPane()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));

        stage.setScene(new Scene(tabs, 1150, 700));
        stage.setTitle("Інформаційна система лісових інспекцій");
        stage.show();

        Platform.runLater(() -> run(this::refreshAll));
    }

    private Pane plotPane() {
        plotTable.getColumns().setAll(
                column("ID", "id"),
                column("Код", "code"),
                column("Назва", "name"),
                column("Тип лісу", "forestType"),
                column("Площа, га", "areaHa"),
                column("Чутливість", "sensitivityLevel"));

        TextField code = new TextField();
        TextField name = new TextField();
        TextField forestType = new TextField();
        TextField area = new TextField();
        ComboBox<Integer> sensitivity = levelBox();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> run(() -> {
            api.addPlot(readPlot(null, code, name, forestType, area, sensitivity));
            refreshPlots();
        }));
        update.setOnAction(e -> run(() -> {
            ForestPlot current = selected(plotTable);
            api.updatePlot(readPlot(current.getId(), code, name, forestType, area, sensitivity));
            refreshPlots();
        }));
        delete.setOnAction(e -> run(() -> {
            api.deletePlot(selected(plotTable).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> run(this::refreshPlots));

        plotTable.getSelectionModel().selectedItemProperty().addListener((obs, old, p) -> {
            if (p != null) {
                code.setText(p.getCode());
                name.setText(p.getName());
                forestType.setText(p.getForestType());
                area.setText(String.valueOf(p.getAreaHa()));
                sensitivity.setValue(p.getSensitivityLevel());
            }
        });

        VBox form = new VBox(8,
                new Label("Код ділянки"), code,
                new Label("Назва"), name,
                new Label("Тип лісу"), forestType,
                new Label("Площа, га"), area,
                new Label("Чутливість ділянки (1–5)"), sensitivity,
                new HBox(8, add, update, delete, refresh));
        return layout(plotTable, form);
    }

    private Pane observationPane() {
        observationTable.getColumns().setAll(
                column("ID", "id"),
                column("ID ділянки", "plotId"),
                column("Дата", "observedAt"),
                column("Спостерігач", "observer"),
                column("Категорія", "category"),
                column("Площа ураження, га", "affectedAreaHa"),
                column("Опис", "description"));

        TextField plotId = new TextField();
        TextField date = new TextField();
        date.setPromptText("РРРР-ММ-ДД");
        TextField observer = new TextField();
        TextField category = new TextField();
        TextField area = new TextField();
        TextField description = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Button importCsv = new Button("Імпортувати з CSV");

        add.setOnAction(e -> run(() -> {
            api.addObservation(readObservation(null, plotId, date, observer, category, area, description));
            refreshObservations();
        }));
        update.setOnAction(e -> run(() -> {
            Observation current = selected(observationTable);
            api.updateObservation(readObservation(current.getId(), plotId, date, observer, category, area, description));
            refreshObservations();
        }));
        delete.setOnAction(e -> run(() -> {
            api.deleteObservation(selected(observationTable).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> run(this::refreshObservations));
        importCsv.setOnAction(e -> run(this::importObservationsFromCsv));

        observationTable.getSelectionModel().selectedItemProperty().addListener((obs, old, o) -> {
            if (o != null) {
                plotId.setText(String.valueOf(o.getPlotId()));
                date.setText(o.getObservedAt());
                observer.setText(o.getObserver());
                category.setText(o.getCategory());
                area.setText(String.valueOf(o.getAffectedAreaHa()));
                description.setText(o.getDescription());
            }
        });

        VBox form = new VBox(8,
                new Label("ID ділянки"), plotId,
                new Label("Дата спостереження"), date,
                new Label("Спостерігач"), observer,
                new Label("Категорія"), category,
                new Label("Площа ураження, га"), area,
                new Label("Опис"), description,
                new HBox(8, add, update, delete, refresh),
                importCsv);
        return layout(observationTable, form);
    }

    private Pane violationPane() {
        violationTable.getColumns().setAll(
                column("ID", "id"),
                column("ID спостереження", "observationId"),
                column("Тяжкість", "severityLevel"),
                column("Стан", "state"),
                column("F1", "f1"),
                column("F2", "f2"),
                column("F3", "f3"),
                column("Індекс I", "integralIndex"));

        TextField observationId = new TextField();
        ComboBox<Integer> severity = levelBox();
        ComboBox<ViolationState> state = new ComboBox<>(
                FXCollections.observableArrayList(ViolationState.values()));
        Label hint = new Label("Порядок станів: Виявлено → Перевірено → Захід заплановано → Закрито. "
                + "Недопустимі переходи відхиляє сервер.");
        hint.setWrapText(true);

        Button add = new Button("Створити");
        Button update = new Button("Перерахувати");
        Button change = new Button("Змінити стан");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> run(() -> {
            api.addViolation(id(observationId, "ID спостереження"), level(severity, "Тяжкість"));
            refreshViolations();
        }));
        update.setOnAction(e -> run(() -> {
            Violation current = selected(violationTable);
            api.updateViolation(current.getId(), id(observationId, "ID спостереження"), level(severity, "Тяжкість"));
            refreshViolations();
        }));
        change.setOnAction(e -> run(() -> {
            Violation current = selected(violationTable);
            if (state.getValue() == null) {
                throw new IllegalArgumentException("Оберіть новий стан");
            }
            api.changeViolationState(current.getId(), state.getValue());
            refreshViolations();
        }));
        delete.setOnAction(e -> run(() -> {
            api.deleteViolation(selected(violationTable).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> run(this::refreshViolations));

        violationTable.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (v != null) {
                observationId.setText(String.valueOf(v.getObservationId()));
                severity.setValue(v.getSeverityLevel());
                state.setValue(v.getState());
            }
        });

        VBox form = new VBox(8,
                new Label("ID спостереження"), observationId,
                new Label("Тяжкість порушення (1–5)"), severity,
                new Label("Стан"), state,
                hint,
                new HBox(8, add, update),
                new HBox(8, change, delete, refresh));
        return layout(violationTable, form);
    }

    private Pane taskPane() {
        taskTable.getColumns().setAll(
                column("ID", "id"),
                column("ID порушення", "violationId"),
                column("Інспектор", "inspector"),
                column("Опис", "description"),
                column("Дата", "plannedDate"),
                column("Виконано", "done"));

        TextField violationId = new TextField();
        TextField inspector = new TextField();
        TextField description = new TextField();
        TextField date = new TextField();
        date.setPromptText("РРРР-ММ-ДД");
        CheckBox done = new CheckBox("Виконано");

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> run(() -> {
            api.addTask(readTask(null, violationId, inspector, description, date, done));
            refreshTasks();
        }));
        update.setOnAction(e -> run(() -> {
            InspectionTask current = selected(taskTable);
            api.updateTask(readTask(current.getId(), violationId, inspector, description, date, done));
            refreshTasks();
        }));
        delete.setOnAction(e -> run(() -> {
            api.deleteTask(selected(taskTable).getId());
            refreshTasks();
        }));
        refresh.setOnAction(e -> run(this::refreshTasks));

        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, t) -> {
            if (t != null) {
                violationId.setText(String.valueOf(t.getViolationId()));
                inspector.setText(t.getInspector());
                description.setText(t.getDescription());
                date.setText(t.getPlannedDate());
                done.setSelected(t.isDone());
            }
        });

        VBox form = new VBox(8,
                new Label("ID порушення"), violationId,
                new Label("Інспектор"), inspector,
                new Label("Опис завдання"), description,
                new Label("Запланована дата"), date,
                done,
                new HBox(8, add, update, delete, refresh));
        return layout(taskTable, form);
    }

    private ForestPlot readPlot(Long id, TextField code, TextField name, TextField forestType,
                                TextField area, ComboBox<Integer> sensitivity) {
        return new ForestPlot(id, code.getText(), name.getText(), forestType.getText(),
                number(area, "Площа, га"), level(sensitivity, "Чутливість"));
    }

    private Observation readObservation(Long id, TextField plotId, TextField date, TextField observer,
                                        TextField category, TextField area, TextField description) {
        return new Observation(id, id(plotId, "ID ділянки"), date.getText(), observer.getText(),
                category.getText(), number(area, "Площа ураження, га"), description.getText());
    }

    private InspectionTask readTask(Long id, TextField violationId, TextField inspector,
                                    TextField description, TextField date, CheckBox done) {
        return new InspectionTask(id, id(violationId, "ID порушення"), inspector.getText(),
                description.getText(), date.getText(), done.isSelected());
    }

    private void importObservationsFromCsv() throws IOException {
        if (!Files.exists(CSV_PATH)) {
            throw new IllegalArgumentException("Не знайдено файл " + CSV_PATH
                    + ". Запустіть клієнт з каталогу client.");
        }
        api.getObservations();

        CsvObservationReader.Result result = CsvObservationReader.read(CSV_PATH);
        List<String> problems = new ArrayList<>(result.errors());
        int imported = 0;
        for (CsvObservationReader.Row row : result.rows()) {
            try {
                api.addObservation(row.observation());
                imported++;
            } catch (RuntimeException e) {
                problems.add("Рядок " + row.line() + ": " + e.getMessage());
            }
        }
        refreshAll();

        StringBuilder text = new StringBuilder("Імпортовано записів: " + imported
                + "\nПропущено рядків: " + problems.size());
        problems.stream().limit(10).forEach(p -> text.append("\n").append(p));
        Alert info = new Alert(Alert.AlertType.INFORMATION, text.toString(), ButtonType.OK);
        info.setHeaderText("Результат імпорту CSV");
        info.showAndWait();
    }

    private void refreshAll() {
        refreshPlots();
        refreshObservations();
        refreshViolations();
        refreshTasks();
    }

    private void refreshPlots() {
        plotTable.setItems(FXCollections.observableArrayList(api.getPlots()));
    }

    private void refreshObservations() {
        observationTable.setItems(FXCollections.observableArrayList(api.getObservations()));
    }

    private void refreshViolations() {
        violationTable.setItems(FXCollections.observableArrayList(api.getViolations()));
    }

    private void refreshTasks() {
        taskTable.setItems(FXCollections.observableArrayList(api.getTasks()));
    }

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис у таблиці");
        }
        return value;
    }

    private ComboBox<Integer> levelBox() {
        return new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    private int level(ComboBox<Integer> box, String label) {
        if (box.getValue() == null) {
            throw new IllegalArgumentException("Оберіть значення: " + label);
        }
        return box.getValue();
    }

    private long id(TextField field, String label) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    private double number(TextField field, String label) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }

    private void run(UiAction action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning("Помилка операції: " + e.getMessage());
            showError(e.getMessage() == null ? "Невідома помилка" : e.getMessage());
        }
    }

    private void showError(String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        dialog.setHeaderText(null);
        dialog.showAndWait();
    }

    private Pane layout(TableView<?> table, Pane form) {
        form.setPadding(new Insets(10));
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.68);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> column(String title, String property) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

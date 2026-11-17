package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class CsvObservationReader {

    public record Row(int line, Observation observation) { }

    public record Result(List<Row> rows, List<String> errors) { }

    private static final String HEADER =
            "plot_id;observed_at;observer;category;affected_area_ha;description";

    private CsvObservationReader() { }

    public static Result read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Row> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (lines.isEmpty()) {
            errors.add("Файл порожній");
            return new Result(rows, errors);
        }
        String header = lines.get(0).replace("﻿", "").trim();
        if (!HEADER.equals(header)) {
            errors.add("Рядок 1: очікується заголовок " + HEADER);
            return new Result(rows, errors);
        }

        for (int i = 1; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                rows.add(new Row(lineNumber, parse(line)));
            } catch (IllegalArgumentException e) {
                errors.add("Рядок " + lineNumber + ": " + e.getMessage());
            }
        }
        return new Result(rows, errors);
    }

    private static Observation parse(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("має бути 6 полів, знайдено " + parts.length);
        }
        long plotId;
        try {
            plotId = Long.parseLong(parts[0].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("plot_id має бути цілим числом");
        }
        String date = parts[1].trim();
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("дата має бути у форматі РРРР-ММ-ДД");
        }
        String observer = parts[2].trim();
        String category = parts[3].trim();
        if (observer.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("observer і category є обов'язковими");
        }
        double area;
        try {
            area = Double.parseDouble(parts[4].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("affected_area_ha має бути числом (десяткова крапка)");
        }
        if (area < 0) {
            throw new IllegalArgumentException("affected_area_ha не може бути від'ємним");
        }
        return new Observation(null, plotId, date, observer, category, area, parts[5].trim());
    }
}

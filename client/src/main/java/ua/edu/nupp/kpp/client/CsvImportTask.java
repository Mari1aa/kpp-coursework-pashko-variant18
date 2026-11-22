package ua.edu.nupp.kpp.client;

import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фонове читання CSV зі спостереженнями та передавання записів на сервер.
 * Результат успішного виконання — кількість імпортованих записів.
 * Предметну валідацію й роботу з MySQL виконує сервер, тут перевіряється лише формат.
 */
public class CsvImportTask extends Task<Integer> {

    private static final Logger LOG = Logger.getLogger(CsvImportTask.class.getName());

    private final Path path;
    private final HttpForestClient api;
    private final List<String> problems = Collections.synchronizedList(new ArrayList<>());
    private volatile int skipped;

    public CsvImportTask(Path path, HttpForestClient api) {
        this.path = path;
        this.api = api;
    }

    /** Список пропущених рядків із причинами (для повідомлення користувачу). */
    public List<String> getProblems() {
        synchronized (problems) {
            return new ArrayList<>(problems);
        }
    }

    public int getSkipped() {
        return skipped;
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок імпорту CSV: " + path.getFileName());
        updateMessage("Читання файла " + path.getFileName() + "...");
        updateProgress(0, 1);

        CsvObservationReader.Result result;
        try {
            result = CsvObservationReader.read(path);
        } catch (NoSuchFileException e) {
            throw fail("Не знайдено файл " + path + ". Запустіть клієнт з каталогу client.", e);
        } catch (IOException e) {
            throw fail("Не вдалося прочитати файл (потрібне кодування UTF-8): " + e.getMessage(), e);
        }

        if (result.rows().isEmpty() && isFileLevelError(result)) {
            throw fail(result.errors().get(0), null);
        }
        for (String error : result.errors()) {
            addProblem(error);
        }

        int total = result.rows().size();
        if (total == 0) {
            skipped = problems.size();
            updateProgress(1, 1);
            if (skipped == 0) {
                updateMessage("У файлі немає рядків з даними");
            } else {
                updateMessage("Жодного рядка не імпортовано, пропущено: " + skipped);
            }
            LOG.info("Імпорт завершено. Успішно: 0, пропущено: " + skipped);
            return 0;
        }

        // Наявні записи потрібні для виявлення дублікатів; одночасно це перевірка доступності сервера.
        Set<String> existing = new HashSet<>();
        try {
            for (Observation o : api.getObservations()) {
                existing.add(key(o));
            }
        } catch (ServerUnavailableException e) {
            throw fail(e.getMessage(), e);
        }

        int imported = 0;
        int processed = 0;
        for (CsvObservationReader.Row row : result.rows()) {
            if (isCancelled()) {
                LOG.warning("Імпорт скасовано користувачем. Імпортовано: " + imported);
                break;
            }
            Observation o = row.observation();
            if (!existing.add(key(o))) {
                addProblem("Рядок " + row.line() + ": такий запис уже існує (дублікат)");
            } else {
                try {
                    api.addObservation(o);
                    imported++;
                } catch (ServerUnavailableException e) {
                    LOG.warning("Імпорт перервано на рядку " + row.line() + ": сервер недоступний");
                    throw new IOException("Сервер став недоступним на рядку " + row.line()
                            + ". Уже імпортовано: " + imported + ". Перевірте дані в MySQL.", e);
                } catch (RuntimeException e) {
                    addProblem("Рядок " + row.line() + ": " + e.getMessage());
                }
            }
            processed++;
            updateProgress(processed, total);
            updateMessage("Опрацьовано " + processed + " з " + total + " рядків");
        }

        skipped = problems.size();
        LOG.info("Імпорт завершено. Успішно: " + imported + ", пропущено: " + skipped);
        updateMessage("Імпортовано: " + imported + ", пропущено: " + skipped);
        return imported;
    }

    private boolean isFileLevelError(CsvObservationReader.Result result) {
        if (result.errors().isEmpty()) {
            return false;
        }
        String first = result.errors().get(0);
        return first.startsWith("Файл порожній") || first.startsWith("Рядок 1:");
    }

    private IOException fail(String message, Exception cause) {
        LOG.log(Level.WARNING, "Імпорт CSV не виконано: " + message, cause);
        updateMessage(message);
        return new IOException(message, cause);
    }

    private void addProblem(String text) {
        problems.add(text);
        LOG.warning("Рядок пропущено: " + text);
    }

    private static String key(Observation o) {
        return o.getPlotId() + "|" + o.getObservedAt() + "|" + o.getObserver() + "|"
                + o.getCategory() + "|" + o.getAffectedAreaHa() + "|"
                + (o.getDescription() == null ? "" : o.getDescription());
    }
}

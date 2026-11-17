package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InspectionTaskService {

    private static final Logger LOG = Logger.getLogger(InspectionTaskService.class.getName());
    private final InspectionTaskDAO dao = new InspectionTaskDAO();
    private final ViolationDAO violationDAO = new ViolationDAO();

    public long add(InspectionTask t) throws SQLException {
        validate(t);
        try {
            long id = dao.add(t);
            LOG.info("Додано завдання інспекції id=" + id + ", порушення=" + t.getViolationId());
            return id;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка додавання завдання інспекції", e);
            throw e;
        }
    }

    public List<InspectionTask> getAll() throws SQLException {
        return dao.getAll();
    }

    public InspectionTask getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void update(InspectionTask t) throws SQLException {
        if (t.getId() == null || dao.getById(t.getId()) == null) {
            LOG.warning("Оновлення відхилено: завдання не знайдено");
            throw new IllegalArgumentException("Завдання інспекції не знайдено");
        }
        validate(t);
        try {
            dao.update(t);
            LOG.info("Оновлено завдання інспекції id=" + t.getId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка оновлення завдання id=" + t.getId(), e);
            throw e;
        }
    }

    public void delete(long id) throws SQLException {
        try {
            if (!dao.delete(id)) {
                LOG.warning("Видалення відхилено: завдання не знайдено, id=" + id);
                throw new IllegalArgumentException("Завдання інспекції не знайдено");
            }
            LOG.info("Видалено завдання інспекції id=" + id);
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.warning("Видалення відхилено: є пов'язані записи, id=" + id);
            throw new IllegalArgumentException("Неможливо видалити: є пов'язані записи");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка видалення завдання id=" + id, e);
            throw e;
        }
    }

    private void validate(InspectionTask t) throws SQLException {
        try {
            ForestPlotService.require(t.getInspector(), "Інспектор");
            ForestPlotService.require(t.getDescription(), "Опис завдання");
            if (t.getDescription().length() > 500) {
                throw new IllegalArgumentException("Опис задовгий (максимум 500 символів)");
            }
            String date = t.getPlannedDate();
            if (date != null && !date.isBlank()) {
                try {
                    LocalDate.parse(date);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Дата має бути у форматі РРРР-ММ-ДД");
                }
            }
            if (violationDAO.getById(t.getViolationId()) == null) {
                throw new IllegalArgumentException("Порушення не знайдено");
            }
        } catch (IllegalArgumentException e) {
            LOG.warning("Валідація завдання не пройдена: " + e.getMessage());
            throw e;
        }
    }
}
